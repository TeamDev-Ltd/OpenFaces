/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2010, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */

package org.openfaces.testapp.support.QKS174;

import org.openfaces.util.FacesUtil;
import org.openfaces.component.table.AllNodesCollapsed;
import org.openfaces.component.table.DynamicNodeExpansionState;
import org.openfaces.component.table.ExpansionState;
import org.openfaces.component.table.TreePath;
import org.openfaces.testapp.screenshot.ForumMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class QKS174 {
    private List rootMessages = new ArrayList();
    private ExpansionState treeTableExpansionState = new DynamicNodeExpansionState(new AllNodesCollapsed());


    public QKS174() {
        String u1 = "Will Green";
        String u2 = "Jane White";
        String u3 = "Chris Lee";
        String u4 = "Dean Genius";
        String u5 = "Jonh Smile";
        String u6 = "Gary Blackt";
        rootMessages.add(new ForumMessage("JDK 6", new Date(), u1, null, Arrays.asList(new Object[]{
                new ForumMessage("Re: JDK 6", new Date(), u5, null, Arrays.asList(new Object[]{
                        new ForumMessage("System-tray support", new Date(), u4, null, Arrays.asList(new Object[]{
                                new ForumMessage("Re: System-tray support", new Date(), u2, null, null)}))
                })),
                new ForumMessage("LCD-optimized text display", new Date(), u6, null, Arrays.asList(new Object[]{
                        new ForumMessage("Re: LCD-optimized text display", new Date(), u4, null, Arrays.asList(new Object[]{
                                new ForumMessage("Re: LCD-optimized text display", new Date(), u5, null, null),
                                new ForumMessage("Re: LCD-optimized text display", new Date(), u6, null, null)}))
                })),
                new ForumMessage("Re: JDK 6", new Date(), u3, null, null),
                new ForumMessage("JSR 199: Java Compiler API", new Date(), u6, null, Arrays.asList(new Object[]{
                        new ForumMessage("Re: JSR 199: Java Compiler API", new Date(), u1, null, null)
                }))})));
        rootMessages.add(new ForumMessage("Re:Parsing algorithm", new Date(), u1, null, Arrays.asList(new Object[]{
                new ForumMessage("Re: Parsing algorithm", new Date(), u4, null, Arrays.asList(new Object[]{
                        new ForumMessage("Re: Parsing algorithm", new Date(), u3, null, Arrays.asList(new Object[]{
                                new ForumMessage("Re: Parsing algorithm", new Date(), u2, null, null)}))}))})));
        rootMessages.add(new ForumMessage("RE: Design patterns", new Date(), u4, null, Arrays.asList(new Object[]{
                new ForumMessage("Singleton", new Date(), u5, null, Arrays.asList(new Object[]{
                        new ForumMessage("Re: Singleton", new Date(), u2, null, null)})),
                new ForumMessage("Re: Design patterns", new Date(), u6, null, null)})));
        rootMessages.add(new ForumMessage("What methodology?", new Date(), u3, null, Arrays.asList(new Object[]{
                new ForumMessage("Re: What methodology?", new Date(), u2, null, null),
                new ForumMessage("Re: What methodology?", new Date(), u6, null, null)})));
        rootMessages.add(new ForumMessage("Scaling an image", new Date(), u2, null, Arrays.asList(new Object[]{
                new ForumMessage("Re: Scaling an image", new Date(), u3, null, Arrays.asList(new Object[]{
                        new ForumMessage("Re: Scaling an image", new Date(), u3, null, Arrays.asList(new Object[]{
                                new ForumMessage("Re: Scaling an image", new Date(), u5, null, null)}))}))})));
        rootMessages.add(new ForumMessage("Create an application", new Date(), u4, null, Arrays.asList(new Object[]{
                new ForumMessage("Re: Create an application", new Date(), u1, null, null)})));
    }


    public List getRootMessages() {
        return rootMessages;
    }

    public void setRootMessages(List rootMessages) {
        this.rootMessages = rootMessages;
    }

    public ExpansionState getTreeTableExpansionState() {
        treeTableExpansionState.setNodeExpanded(new TreePath("message2", new TreePath("message10", null)), true);
        treeTableExpansionState.setNodeExpanded(new TreePath("message10", null), true);
        return treeTableExpansionState;
    }

    public void setTreeTableExpansionState(ExpansionState treeTableExpansionState) {
        this.treeTableExpansionState = treeTableExpansionState;
    }

    public List getNodeChildren() {
        org.openfaces.testapp.screenshot.ForumMessage message = (ForumMessage) FacesUtil.getRequestMapValue("message");
        return message != null ? message.getReplies() : rootMessages;
    }
}
