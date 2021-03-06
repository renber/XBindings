/***********************************************************************************************************************
 *
 * BetterBeansBinding - keeping JavaBeans in sync
 * ==============================================
 *
 * Copyright (C) 2009 by Tidalwave s.a.s. (http://www.tidalwave.it)
 * http://betterbeansbinding.kenai.com
 *
 * This is derived work from BeansBinding: http://beansbinding.dev.java.net
 * BeansBinding is copyrighted (C) by Sun Microsystems, Inc.
 *
 ***********************************************************************************************************************
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ***********************************************************************************************************************
 *
 * $Id: JListAdapterProvider.java 60 2009-04-26 20:47:20Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.swingbinding.adapters;

import org.jdesktop.beansbinding.ext.BeanAdapterProvider;

import org.jdesktop.swingbinding.impl.ListBindingManager;

import java.beans.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;


/**
 * @author Shannon Hickey
 */
public final class JListAdapterProvider implements BeanAdapterProvider {
    private static final String SELECTED_ELEMENT_P = "selectedElement";
    private static final String SELECTED_ELEMENTS_P = "selectedElements";
    private static final String SELECTED_ELEMENT_IA_P = SELECTED_ELEMENT_P +
        "_IGNORE_ADJUSTING";
    private static final String SELECTED_ELEMENTS_IA_P = SELECTED_ELEMENTS_P +
        "_IGNORE_ADJUSTING";

    private static List<Object> getSelectedElements(JList list) {
        assert list != null;

        ListSelectionModel selectionModel = list.getSelectionModel();
        int min = selectionModel.getMinSelectionIndex();
        int max = selectionModel.getMaxSelectionIndex();

        List<Object> newSelection;

        if ((min < 0) || (max < 0)) {
            return new ArrayList<Object>(0);
        }

        ArrayList<Object> elements = new ArrayList<Object>(max - min + 1);

        for (int i = min; i <= max; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                elements.add(getElement(list, i));
            }
        }

        return elements;
    }

    private static Object getSelectedElement(JList list) {
        assert list != null;

        // PENDING(shannonh) - more cases to consider
        int index = list.getSelectionModel().getLeadSelectionIndex();
        index = list.getSelectionModel().isSelectedIndex(index) ? index
                                                                : list.getSelectionModel()
                                                                      .getMinSelectionIndex();

        if (index == -1) {
            return null;
        }

        return getElement(list, index);
    }

    private static Object getElement(JList list, int index) {
        ListModel model = list.getModel();

        return (model instanceof ListBindingManager)
        ? ((ListBindingManager) model).getElement(index)
        : model.getElementAt(index);
    }

    public boolean providesAdapter(Class<?> type, String property) {
        if (!JList.class.isAssignableFrom(type)) {
            return false;
        }

        property = property.intern();

        return (property == SELECTED_ELEMENT_P) ||
        (property == SELECTED_ELEMENT_IA_P) ||
        (property == SELECTED_ELEMENTS_P) ||
        (property == SELECTED_ELEMENTS_IA_P);
    }

    public Object createAdapter(Object source, String property) {
        if (!providesAdapter(source.getClass(), property)) {
            throw new IllegalArgumentException();
        }

        return new Adapter((JList) source, property);
    }

    public Class<?> getAdapterClass(Class<?> type) {
        return JList.class.isAssignableFrom(type)
        ? JListAdapterProvider.Adapter.class : null;
    }

    public final class Adapter extends BeanAdapterBase {
        private JList list;
        private Handler handler;
        private Object cachedElementOrElements;

        private Adapter(JList list, String property) {
            super(property);
            this.list = list;
        }

        private boolean isPlural() {
            return (property == SELECTED_ELEMENTS_P) ||
            (property == SELECTED_ELEMENTS_IA_P);
        }

        public Object getSelectedElement() {
            return JListAdapterProvider.getSelectedElement(list);
        }

        public Object getSelectedElement_IGNORE_ADJUSTING() {
            return getSelectedElement();
        }

        public List<Object> getSelectedElements() {
            return JListAdapterProvider.getSelectedElements(list);
        }

        public List<Object> getSelectedElements_IGNORE_ADJUSTING() {
            return getSelectedElements();
        }

        protected void listeningStarted() {
            handler = new Handler();
            cachedElementOrElements = isPlural() ? getSelectedElements()
                                                 : getSelectedElement();
            list.addPropertyChangeListener("model", handler);
            list.addPropertyChangeListener("selectionModel", handler);
            list.getSelectionModel().addListSelectionListener(handler);
        }

        protected void listeningStopped() {
            list.getSelectionModel().removeListSelectionListener(handler);
            list.removePropertyChangeListener("model", handler);
            list.removePropertyChangeListener("selectionModel", handler);
            cachedElementOrElements = null;
            handler = null;
        }

        private class Handler implements ListSelectionListener,
            PropertyChangeListener {
            private void listSelectionChanged() {
                Object oldElementOrElements = cachedElementOrElements;
                cachedElementOrElements = isPlural() ? getSelectedElements()
                                                     : getSelectedElement();
                firePropertyChange(oldElementOrElements, cachedElementOrElements);
            }

            public void valueChanged(ListSelectionEvent e) {
                if (((property == SELECTED_ELEMENT_IA_P) ||
                        (property == SELECTED_ELEMENTS_IA_P)) &&
                        e.getValueIsAdjusting()) {
                    return;
                }

                listSelectionChanged();
            }

            public void propertyChange(PropertyChangeEvent pce) {
                String propertyName = pce.getPropertyName();

                if (propertyName == "selectionModel") {
                    ((ListSelectionModel) pce.getOldValue()).removeListSelectionListener(handler);
                    ((ListSelectionModel) pce.getNewValue()).addListSelectionListener(handler);
                    listSelectionChanged();
                } else if (propertyName == "model") {
                    listSelectionChanged();
                }
            }
        }
    }
}
