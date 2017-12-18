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
 * $Id: MethodExpression.java 49 2009-04-25 22:31:22Z fabriziogiudici $
 * 
 **********************************************************************************************************************/
package org.jdesktop.el;

/**
 * An <code>Expression</code> that refers to a method on an object.
 *
 * <p>The {@link ExpressionFactory#createMethodExpression} method
 * can be used to parse an expression string and return a concrete instance
 * of <code>MethodExpression</code> that encapsulates the parsed expression.
 * The {@link FunctionMapper} is used at parse time, not evaluation time, 
 * so one is not needed to evaluate an expression using this class.  
 * However, the {@link ELContext} is needed at evaluation time.</p>
 *
 * <p>The {@link #getMethodInfo} and {@link #invoke} methods will evaluate the 
 * expression each time they are called. The {@link ELResolver} in the 
 * <code>ELContext</code> is used to resolve the top-level variables and to 
 * determine the behavior of the <code>.</code> and <code>[]</code> 
 * operators. For any of the two methods, the {@link ELResolver#getValue} 
 * method is used to resolve all properties up to but excluding the last 
 * one. This provides the <code>base</code> object on which the method
 * appears. If the <code>base</code> object is null, a 
 * <code>PropertyNotFoundException</code> must be thrown.
 * At the last resolution, 
 * the final <code>property</code> is then coerced to a <code>String</code>,
 * which provides the name of the method to be found. A method matching the 
 * name and expected parameters provided at parse time is found and it is 
 * either queried or invoked (depending on the method called on this
 * <code>MethodExpression</code>).</p>
 *
 * <p>See the notes about comparison, serialization and immutability in 
 * the {@link Expression} javadocs.
 *
 * @see ELResolver
 * @see Expression
 * @see ExpressionFactory
 * @since JSP 2.1
 */
public abstract class MethodExpression extends Expression
{
    // Evaluation
    
    /**
     * Evaluates the expression relative to the provided context, and
     * returns information about the actual referenced method.
     *
     * @param context The context of this evaluation
     * @return an instance of <code>MethodInfo</code> containing information
     *     about the method the expression evaluated to.
     * @throws NullPointerException if context is <code>null</code>
     * @throws PropertyNotFoundException if one of the property
     *     resolutions failed because a specified variable or property 
     *     does not exist or is not readable.
     * @throws MethodNotFoundException if no suitable method can be found.
     * @throws ELException if an exception was thrown while performing
     *     property or variable resolution. The thrown exception
     *     must be included as the cause property of this exception, if
     *     available.
     */
    public abstract MethodInfo getMethodInfo(ELContext context);
    
    /**
     * If a String literal is specified as the expression, returns the 
     * String literal coerced to the expected return type of the method 
     * signature. An <code>ELException</code> is thrown if 
     * <code>expectedReturnType</code> is void or if the coercion of the String literal 
     * to the <code>expectedReturnType</code> yields an error (see Section "1.16 Type
     * Conversion" of the EL specification).
     * 
     * If not a String literal, evaluates the expression 
     * relative to the provided context, invokes the method that was 
     * found using the supplied parameters, and returns the result of 
     * the method invocation.
     *
     * Any parameters passed to this method is ignored if isLiteralText()
     * is true.
     *
     * @param context The context of this evaluation.
     * @param params The parameters to pass to the method, or
     *     <code>null</code> if no parameters.
     * @return the result of the method invocation (<code>null</code> if
     *     the method has a <code>void</code> return type).
     * @throws NullPointerException if context is <code>null</code>
     * @throws PropertyNotFoundException if one of the property
     *     resolutions failed because a specified variable or property 
     *     does not exist or is not readable.
     * @throws MethodNotFoundException if no suitable method can be found.
     * @throws ELException if a String literal is specified and
     * expectedReturnType of the MethodExpression is void or if the coercion of the String literal 
     * to the expectedReturnType yields an error (see Section "1.16 Type
     * Conversion").
     * @throws ELException if 
     * an exception was thrown while performing
     *     property or variable resolution. The thrown exception must be
     *     included as the cause property of this exception, if
     *     available.  If the exception thrown is an
     *     <code>InvocationTargetException</code>, extract its
     *     <code>cause</code> and pass it to the
     *     <code>ELException</code> constructor.
     */
    public abstract Object invoke(ELContext context, Object[] params);
}
