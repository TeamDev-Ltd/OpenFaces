/*
 * OpenFaces - JSF Component Library 3.0
 * Copyright (C) 2007-2010, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.openfaces.ajax;

import org.openfaces.org.json.JSONException;
import org.openfaces.org.json.JSONObject;
import org.openfaces.renderkit.AjaxPortionRenderer;
import org.openfaces.util.AjaxUtil;
import org.openfaces.util.AnonymousFunction;
import org.openfaces.util.FunctionCallScript;
import org.openfaces.util.InitScript;
import org.openfaces.util.Rendering;
import org.openfaces.util.Script;
import org.openfaces.util.ScriptBuilder;
import org.openfaces.util.UtilPhaseListener;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContextWrapper;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Pikhulya
 */
public class PartialViewContext extends PartialViewContextWrapper {
    private javax.faces.context.PartialViewContext wrapped;

    public PartialViewContext(javax.faces.context.PartialViewContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public PartialResponseWriter getPartialResponseWriter() {
        PartialResponseWriter originalWriter = super.getPartialResponseWriter();
        return new PartialResponseWriterWrapper(originalWriter) {
            @Override
            public void endDocument() throws IOException {
                renderAdditionalPartialResponse();
                super.endDocument();
            }
        };
    }

    @Override
    public javax.faces.context.PartialViewContext getWrapped() {
        return wrapped;
    }

    @Override
    public void setPartialRequest(boolean isPartialRequest) {
        wrapped.setPartialRequest(isPartialRequest);
    }

    @Override
    public void processPartial(PhaseId phaseId) {
        super.processPartial(phaseId);
        if (isAjaxRequest()) {
            if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                UtilPhaseListener.processAjaxExecutePhase(FacesContext.getCurrentInstance());
            }
        }

    }

    @Override
    public boolean isRenderAll() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (AjaxUtil.isAjaxPortionRequest(context))
            return false;
        return super.isRenderAll();
    }

    @Override
    public Collection<String> getRenderIds() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (AjaxUtil.isAjaxPortionRequest(context))
            return Collections.emptyList();
        return super.getRenderIds();
    }

    @Override
    public Collection<String> getExecuteIds() {
        Collection<String> executeIds = super.getExecuteIds();

        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        boolean openFacesAjax = externalContext.getRequestParameterMap().containsKey(AjaxUtil.AJAX_REQUEST_MARKER);
        if (!openFacesAjax) return executeIds;

        boolean executeRenderedComponents = CommonAjaxViewRoot.extractExecuteRenderedComponents(context);
        if (executeRenderedComponents) {
            executeIds = new HashSet<String>(executeIds);
            executeIds.addAll(super.getRenderIds());
        }
        return executeIds;
    }

    public static void renderAdditionalPartialResponse() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            renderAdditionalPartialResponse(context);
        } catch (IOException e) {
            throw new FacesException(e);
        }
    }

    private static void renderAdditionalPartialResponse(FacesContext context) throws IOException {
        renderAjaxPortions(context);
        renderAjaxInitScripts(context);
    }

    private static void renderAjaxInitScripts(FacesContext context) throws IOException {
        InitScript script = getCombinedAjaxInitScripts(context);
        if (script == null) return;

        javax.faces.context.PartialViewContext partialViewContext = context.getPartialViewContext();
        PartialResponseWriter partialWriter = partialViewContext.getPartialResponseWriter();
        partialWriter.startEval();
        partialWriter.write(
                new FunctionCallScript("O$._runScript", script.getScript(), script.getJsFiles()).toString()
        );
        partialWriter.endEval();
    }

    private static InitScript getCombinedAjaxInitScripts(FacesContext context) {
        ScriptBuilder sb = new ScriptBuilder();
        Set<String> jsFiles = new LinkedHashSet<String>();
        List<InitScript> initScripts = Rendering.getAjaxInitScripts(context);
        if (initScripts.isEmpty()) return null;
        boolean semicolonNeeded = false;
        for (InitScript initScript : initScripts) {
            Script script = initScript.getScript();
            if (semicolonNeeded) sb.semicolon();
            sb.append(script);
            semicolonNeeded = true;
            String[] files = initScript.getJsFiles();
            if (files != null)
                jsFiles.addAll(Arrays.asList(files));
        }
        initScripts.clear();
        // remove the possible null references, which are normally allowed to present, from js library list
        jsFiles.remove(null);
        InitScript script = new InitScript(new AnonymousFunction(sb), jsFiles.toArray(new String[jsFiles.size()]));
        return script;
    }

    private static void renderAjaxPortions(FacesContext context) throws IOException {
        List<String> updatePortions = AjaxUtil.getAjaxPortionNames(context);
        if (updatePortions.isEmpty()) return;
        ExternalContext externalContext = context.getExternalContext();
        String renderParam = externalContext.getRequestParameterMap().get(
                    javax.faces.context.PartialViewContext.PARTIAL_RENDER_PARAM_NAME);
        String[] renderIds = renderParam.split("[ \t]+");
        if (renderIds.length != 1) throw new RuntimeException("There should be one target component but was: " + renderIds.length);
        UIComponent component = UtilPhaseListener.findComponentById(context.getViewRoot(), renderIds[0],
                false, true, true);

        RenderKitFactory factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = factory.getRenderKit(context, context.getViewRoot().getRenderKitId());
        Renderer renderer = renderKit.getRenderer(component.getFamily(), component.getRendererType());
        JSONObject customJSONParam = AjaxUtil.getCustomJSONParam(context);
        AjaxPortionRenderer ajaxComponentRenderer = (AjaxPortionRenderer) renderer;
        for (String portionName : updatePortions) {
            StringBuilder portionOutput;
            JSONObject responseData;
            StringWriter stringWriter = new StringWriter();
            ResponseWriter originalWriter = CommonAjaxViewRoot.substituteResponseWriter(context, stringWriter);
            try {
                responseData = ajaxComponentRenderer.encodeAjaxPortion(context, component, portionName, customJSONParam);
                portionOutput = new StringBuilder(stringWriter.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } finally {
                CommonAjaxViewRoot.restoreWriter(context, originalWriter);
            }

            StringBuilder rawScriptsBuffer = new StringBuilder();
            StringBuilder rtLibraryScriptsBuffer = new StringBuilder();
            CommonAjaxViewRoot.extractScripts(portionOutput, rawScriptsBuffer, rtLibraryScriptsBuffer);
            if (rtLibraryScriptsBuffer.length() > 0) {
//                initializationScripts.append(rtLibraryScriptsBuffer).append("\n");
            }

            javax.faces.context.PartialViewContext partialViewContext = context.getPartialViewContext();
            PartialResponseWriter partialWriter = partialViewContext.getPartialResponseWriter();
            Map<String, String> extensionAttributes = new HashMap<String, String>();
            extensionAttributes.put("ln", "openfaces");
            extensionAttributes.put("portion", portionName);
            extensionAttributes.put("text", portionOutput.toString());
            extensionAttributes.put("data", responseData != null ? responseData.toString() : "null");
            extensionAttributes.put("scripts", rawScriptsBuffer.toString());

            partialWriter.startExtension(extensionAttributes);
            partialWriter.endExtension();
        }
    }


}