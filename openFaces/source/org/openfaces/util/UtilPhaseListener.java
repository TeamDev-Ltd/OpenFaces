/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2014, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.util;

import org.openfaces.component.ajax.DefaultProgressMessage;
import org.openfaces.component.table.AbstractTable;
import org.openfaces.component.table.Columns;
import org.openfaces.component.table.impl.DynamicColumn;
import org.openfaces.event.AjaxActionEvent;
import org.openfaces.renderkit.ajax.DefaultProgressMessageRenderer;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Pikhulya
 */
public class UtilPhaseListener extends PhaseListenerBase {
    private static final String FOCUSED_COMPONENT_ID_KEY = UtilPhaseListener.class.getName() + ".focusedComponentId";
    private static final String FOCUS_TRACKER_FIELD_ID = "o::defaultFocus";
    private static final String FORCE_UTIL_JS_CONTEXT_PARAM = "org.openfaces.forceIncludingUtilJs";
    private static final String AUTO_FOCUS_TRACKING_CONTEXT_PARAM = "org.openfaces.autoSaveFocus";
    private static final String DISABLED_CONTEXT_MENU_CONTEXT_PARAM = "org.openfaces.disabledContextMenu";

    private static final String SCROLL_POS_KEY = UtilPhaseListener.class.getName() + ".pageScrollPos";
    private static final String SCROLL_POS_TRACKER_FIELD_ID = "o::defaultScrollPosition";
    private static final String AUTO_SCROLL_POS_TRACKING_CONTEXT_PARAM = "org.openfaces.autoSaveScrollPos";
    private static final String SUBMISSION_AJAX_INACTIVITY_TIMEOUT_CONTEXT_PARAM = "org.openfaces.submissionAjaxInactivityTimeout";
    private static final long DEFAULT_SUBMISSION_AJAX_INACTIVITY_TIMEOUT = 2000;

    public void beforePhase(PhaseEvent event) {
    }

    public void afterPhase(PhaseEvent event) {
        if (checkPortletMultipleNotifications(event, false))
            return;

        FacesContext context = event.getFacesContext();
        PhaseId phaseId = event.getPhaseId();
        if (phaseId.equals(PhaseId.RENDER_RESPONSE)) {
//            appendHeaderContent(context);
        } else if (phaseId.equals(PhaseId.APPLY_REQUEST_VALUES)) {
            decodeFocusTracking(context);
            decodeScrollPosTracking(context);
        }
    }

    public static void appendHeaderContent(FacesContext context) {
        List<String> renderedJsLinks = Resources.getRenderedJsLinks(context);
        String utilJs = Resources.utilJsURL(context);
        boolean renderFocusScript = isAutoFocusTrackingEnabled(context);
        boolean renderScrollingScript = isAutoScrollPosTrackingEnabled(context);
        boolean renderContextMenuScript = isDisabledContextMenuEnabled(context);
        if (!renderedJsLinks.contains(utilJs)) {
            if (renderFocusScript ||
                    renderScrollingScript ||
                    renderContextMenuScript ||
                    getForceIncludingUtilJs(context)) {
                Resources.addHeaderResource(context, Resources.UTIL_JS_PATH, Resources.LIBRARY_NAME);
            }
        }
        if (renderFocusScript)
            Resources.addHeaderInitScript(context, encodeFocusTracking(context));
        if (renderScrollingScript)
            Resources.addHeaderInitScript(context, encodeScrollPosTracking(context));

        if (renderContextMenuScript)
            Rendering.appendOnLoadScript(context, encodeDisabledContextMenu(context));
        encodeAjaxProgressMessage(context);
    }

    private static void encodeAjaxProgressMessage(FacesContext context) {
        DefaultProgressMessage defaultProgressMessage = new DefaultProgressMessage();
        renderProgressMessage(context, defaultProgressMessage);
    }

    private static void renderProgressMessage(FacesContext context, DefaultProgressMessage defaultProgressMessage) {
        try {
            defaultProgressMessage.encodeAll(context);
        } catch (IOException e) {
            throw new FacesException(e);
        }
    }

    private static boolean getForceIncludingUtilJs(FacesContext context) {
        return Rendering.getBooleanContextParam(context, FORCE_UTIL_JS_CONTEXT_PARAM);
    }

    private static boolean isAutoFocusTrackingEnabled(FacesContext context) {
        return Rendering.getBooleanContextParam(context, AUTO_FOCUS_TRACKING_CONTEXT_PARAM);
    }

    private static boolean isDisabledContextMenuEnabled(FacesContext context) {
        return Rendering.getBooleanContextParam(context, DISABLED_CONTEXT_MENU_CONTEXT_PARAM);
    }

    private static boolean isAutoScrollPosTrackingEnabled(FacesContext context) {
        return Rendering.getBooleanContextParam(context, AUTO_SCROLL_POS_TRACKING_CONTEXT_PARAM);
    }

    private static Script encodeFocusTracking(FacesContext facesContext) {
        ExternalContext externalContext = facesContext.getExternalContext();
        Map requestMap = externalContext.getRequestMap();
        String focusedComponentId = (String) requestMap.get(FOCUSED_COMPONENT_ID_KEY);
        return new ScriptBuilder().functionCall("O$.initDefaultFocus",
                FOCUS_TRACKER_FIELD_ID,
                focusedComponentId != null ? focusedComponentId : null).semicolon();
    }

    private static Script encodeScrollPosTracking(FacesContext facesContext) {
        ExternalContext externalContext = facesContext.getExternalContext();
        Map requestMap = externalContext.getRequestMap();
        String scrollPos = (String) requestMap.get(SCROLL_POS_KEY);
        return new ScriptBuilder().functionCall("O$.initDefaultScrollPosition",
                SCROLL_POS_TRACKER_FIELD_ID,
                scrollPos != null ? scrollPos : null).semicolon();
    }

    private static Script encodeDisabledContextMenu(FacesContext facesContext) {
        return new RawScript("O$.disabledContextMenuFor(document);");
    }

    public static void encodeFormSubmissionAjaxInactivityTimeout(FacesContext context) {
        ExternalContext externalContext = context.getExternalContext();
        String paramStr = externalContext.getInitParameter(SUBMISSION_AJAX_INACTIVITY_TIMEOUT_CONTEXT_PARAM);

        long inactivityTimeout = DEFAULT_SUBMISSION_AJAX_INACTIVITY_TIMEOUT;
        if (paramStr != null) {
            try {
                final long parameterValue = Long.parseLong(paramStr);
                inactivityTimeout = Math.abs(parameterValue);
            } catch (NumberFormatException e) {
                externalContext.log("Invalid value specified for context parameter named " + SUBMISSION_AJAX_INACTIVITY_TIMEOUT_CONTEXT_PARAM
                        + ": it must be a number");
            }
        }

        final ScriptBuilder script = new ScriptBuilder("O$.setSubmissionAjaxInactivityTimeout(" + inactivityTimeout + ");");

        Rendering.appendOnLoadScript(context, script);
    }

    private void decodeFocusTracking(FacesContext facesContext) {
        if (!isAutoFocusTrackingEnabled(facesContext))
            return;
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
        String focusedComponentId = requestParameterMap.get(FOCUS_TRACKER_FIELD_ID);
        Map<String, Object> requestMap = externalContext.getRequestMap();
        requestMap.put(FOCUSED_COMPONENT_ID_KEY, focusedComponentId);
    }

    private void decodeScrollPosTracking(FacesContext facesContext) {
        if (!isAutoScrollPosTrackingEnabled(facesContext))
            return;
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
        String focusedComponentId = requestParameterMap.get(SCROLL_POS_TRACKER_FIELD_ID);
        Map<String, Object> requestMap = externalContext.getRequestMap();
        requestMap.put(SCROLL_POS_KEY, focusedComponentId);
    }


    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
