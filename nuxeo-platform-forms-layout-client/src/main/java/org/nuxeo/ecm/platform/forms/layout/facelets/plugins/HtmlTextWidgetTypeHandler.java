/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: HtmlTextWidgetTypeHandler.java 30416 2008-02-21 19:10:37Z atchertchian $
 */

package org.nuxeo.ecm.platform.forms.layout.facelets.plugins;

import java.util.Arrays;

import javax.faces.component.html.HtmlOutputText;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.LeafFaceletHandler;
import org.nuxeo.ecm.platform.ui.web.component.editor.UIHtmlEditor;
import org.nuxeo.ecm.platform.ui.web.component.seam.UIHtmlText;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.CompositeFaceletHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;

/**
 * Html text widget.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class HtmlTextWidgetTypeHandler extends AbstractWidgetTypeHandler {

    private static final long serialVersionUID = 1495841177711755669L;

    @Override
    public FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers)
            throws WidgetException {
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, tagConfig);
        String mode = widget.getMode();
        String widgetId = widget.getId();
        String widgetName = widget.getName();
        String widgetTagConfigId = widget.getTagConfigId();
        TagAttributes attributes;
        if (BuiltinWidgetModes.isLikePlainMode(mode)) {
            // use attributes without id
            attributes = helper.getTagAttributes(widget);
        } else {
            if (BuiltinWidgetModes.EDIT.equals(mode)) {
                // exclude styleClass, it needs a specific css class also, see
                // below
                attributes = helper.getTagAttributes(widget,
                        Arrays.asList("styleClass"), true);
                attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                        helper.createAttribute("id", widgetId));
            } else {
                attributes = helper.getTagAttributes(widgetId, widget);
            }
        }
        FaceletHandler leaf = null;
        if (subHandlers != null) {
            leaf = new CompositeFaceletHandler(subHandlers);
        } else {
            leaf = new LeafFaceletHandler();
        }
        if (BuiltinWidgetModes.EDIT.equals(mode)) {
            ComponentHandler input = helper.getHtmlComponentHandler(
                    widgetTagConfigId, attributes, leaf,
                    UIHtmlEditor.COMPONENT_TYPE, null);
            String msgId = helper.generateMessageId(widgetName);
            ComponentHandler message = helper.getMessageComponentHandler(
                    widgetTagConfigId, msgId, widgetId, null);
            FaceletHandler[] handlers = { input, message };
            return new CompositeFaceletHandler(handlers);
        } else {
            // default on text for other modes, do not escape
            TagAttribute escape = helper.createAttribute("escape", "false");
            attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                    escape);
            String styleClass = (String) widget.getProperty("styleClass");
            TagAttribute styleClassAttr = helper.createAttribute("styleClass",
                    StringUtils.isBlank(styleClass) ? "textBlock"
                            : "textBlock " + styleClass);
            attributes = FaceletHandlerHelper.addTagAttribute(attributes,
                    styleClassAttr);
            ComponentHandler output = helper.getHtmlComponentHandler(
                    widgetTagConfigId, attributes, leaf,
                    HtmlOutputText.COMPONENT_TYPE, null);
            if (BuiltinWidgetModes.PDF.equals(mode)) {
                // add a surrounding p:html tag handler
                return helper.getHtmlComponentHandler(widgetTagConfigId,
                        new TagAttributes(new TagAttribute[0]), output,
                        UIHtmlText.class.getName(), null);
            } else {
                return output;
            }
        }
    }
}
