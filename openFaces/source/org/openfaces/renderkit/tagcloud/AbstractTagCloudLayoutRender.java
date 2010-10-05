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

package org.openfaces.renderkit.tagcloud;

import org.openfaces.component.tagcloud.Layout;
import org.openfaces.component.tagcloud.TagCloud;
import org.openfaces.component.tagcloud.TagCloudItem;
import org.openfaces.renderkit.RendererBase;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

/**
 * @author : roman.nikolaienko
 */
public abstract class AbstractTagCloudLayoutRender extends RendererBase {

    public void renderLayout(FacesContext context, UIComponent component) throws IOException {        
        TagCloud cloud = (TagCloud) component;
        
        List<TagCloudItem> items = cloud.itemsToTheList(context);

        TagsOrderingStrategy orderingStrategy = AbstractTagsOrderingStrategy.getInstance(cloud.getOrder());        
        orderingStrategy.order(items);

        renderItems(context, cloud, items);
    }

    protected abstract void renderItems(FacesContext context, TagCloud cloud, List<TagCloudItem> items)
            throws IOException;

    public static AbstractTagCloudLayoutRender getInstance(Layout layout) {
        switch (layout) {
            case RECTANGLE:
                return new TagCloudRectangleLayoutRender();
            case VERTICAL:
                return new TagCloudVerticalLayoutRender();
            case OVAL:
                return new TagCloudOvalLayoutRender();
        }
        throw new FacesException("Wrong layout type: " + layout);
    }


}
