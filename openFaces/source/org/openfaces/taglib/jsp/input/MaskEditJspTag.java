/*
 * OpenFaces - JSF Component Library 2.0
 * Copyright (C) 2007-2014, TeamDev Ltd.
 * licensing@openfaces.org
 * Unless agreed in writing the contents of this file are subject to
 * the GNU Lesser General Public License Version 2.1 (the "LGPL" License).
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * Please visit http://openfaces.org/licensing/ for more details.
 */
package org.openfaces.taglib.jsp.input;

import org.openfaces.taglib.jsp.OUIInputTextJspTag;

import javax.el.ValueExpression;

public class MaskEditJspTag extends OUIInputTextJspTag {

    public MaskEditJspTag() {
        super(new org.openfaces.taglib.internal.input.MaskEditTag());
    }

    public void setMask(ValueExpression mask) {
        getDelegate().setPropertyValue("mask", mask);
    }

    public void setBlank(ValueExpression blank) {
        getDelegate().setPropertyValue("blank", blank);
    }

    public void setDictionary(ValueExpression dictionary) {
        getDelegate().setPropertyValue("dictionary", dictionary);
    }

    public void setBlankVisible(ValueExpression blankVisible) {
        getDelegate().setPropertyValue("blankVisible", blankVisible);
    }


}
