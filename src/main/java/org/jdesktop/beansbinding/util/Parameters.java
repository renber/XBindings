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
 * $Id: Parameters.java 72 2009-06-12 19:48:25Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.beansbinding.util;


/***********************************************************************************************************************
 *
 * An utility class for validating parameters.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public final class Parameters {

    private Parameters() {
    }

    public static void checkNotNull(final Object object, final String name) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("%s cannot be null", name));
        }
    }
}
