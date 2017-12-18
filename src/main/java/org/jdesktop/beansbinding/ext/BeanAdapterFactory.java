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
 * $Id: BeanAdapterFactory.java 60 2009-04-26 20:47:20Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.beansbinding.ext;

import java.beans.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.lang.ref.WeakReference;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


public final class BeanAdapterFactory {
    private static final BeanAdapterFactory INSTANCE = new BeanAdapterFactory();
    private final Map<Object, List<VendedAdapter>> vendedAdapters;
    private final List<BeanAdapterProvider> providers;
    private final Set<ClassLoader> classLoaders;
    private final Set<URL> serviceURLs;

    public BeanAdapterFactory() {
        this.providers = new ArrayList<BeanAdapterProvider>();
        classLoaders = new HashSet<ClassLoader>();
        serviceURLs = new HashSet<URL>();
        vendedAdapters = new WeakHashMap<Object, List<VendedAdapter>>();
    }

    public static Object getAdapter(Object source, String property) {
        return INSTANCE.getAdapter0(source, property);
    }

    public static List<PropertyDescriptor> getAdapterPropertyDescriptors(
        Class<?> type) {
        return INSTANCE.getAdapterPropertyDescriptors0(type);
    }

    private void loadProvidersIfNecessary() {
        ClassLoader currentLoader = Thread.currentThread()
                                          .getContextClassLoader();

        if (!classLoaders.contains(currentLoader)) {
            classLoaders.add(currentLoader);
            loadProviders(currentLoader);
        }
    }

    private void loadProviders(ClassLoader classLoader) {
        // PENDING: this needs to be rewriten in terms of ServiceLoader
        String serviceName = "META-INF/services/" +
            BeanAdapterProvider.class.getName();

        try {
            Enumeration<URL> urls = classLoader.getResources(serviceName);

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();

                if (!serviceURLs.contains(url)) {
                    serviceURLs.add(url);
                    addProviders(url);
                }
            }
        } catch (IOException ex) {
        }
    }

    private void addProviders(URL url) {
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = url.openStream();
            reader = new BufferedReader(new InputStreamReader(inputStream,
                        "utf-8"));

            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    providers.add((BeanAdapterProvider) Class.forName(line)
                                                             .newInstance());
                } catch (IllegalAccessException ex) {
                } catch (InstantiationException ex) {
                } catch (ClassNotFoundException ex) {
                }
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IOException ex) {
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
            }
        }
    }

    public Object getAdapter0(Object source, String property) {
        if ((source == null) || (property == null)) {
            throw new IllegalArgumentException();
        }

        loadProvidersIfNecessary();
        property = property.intern();

        BeanAdapterProvider provider = getProvider(source, property);

        if (provider != null) {
            List<VendedAdapter> adapters = vendedAdapters.get(source);

            if (adapters != null) {
                for (int i = adapters.size() - 1; i >= 0; i--) {
                    VendedAdapter vendedAdapter = adapters.get(i);
                    Object adapter = vendedAdapter.getAdapter();

                    if (adapter == null) {
                        vendedAdapters.remove(i);
                    } else if ((vendedAdapter.getProvider() == provider) &&
                            (vendedAdapter.getProperty() == property)) {
                        return adapter;
                    }
                }
            } else {
                adapters = new ArrayList<VendedAdapter>(1);
                vendedAdapters.put(source, adapters);
            }

            Object adapter = provider.createAdapter(source, property);
            adapters.add(new VendedAdapter(property, provider, adapter));

            return adapter;
        }

        return null;
    }

    private BeanAdapterProvider getProvider(Object source, String property) {
        Class<?> type = source.getClass();

        for (BeanAdapterProvider provider : providers) {
            if (provider.providesAdapter(type, property)) {
                return provider;
            }
        }

        return null;
    }

    private static BeanInfo getBeanInfo(Class<?> type) {
        try {
            return Introspector.getBeanInfo(type);
        } catch (IntrospectionException ie) {
            return null;
        }
    }

    private List<PropertyDescriptor> getAdapterPropertyDescriptors0(
        Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must be non-null");
        }

        loadProvidersIfNecessary();

        ArrayList<PropertyDescriptor> des = new ArrayList<PropertyDescriptor>();

        for (BeanAdapterProvider provider : providers) {
            Class<?> pdType = provider.getAdapterClass(type);

            if (pdType != null) {
                BeanInfo info = getBeanInfo(pdType);

                if (info != null) {
                    PropertyDescriptor[] pds = info.getPropertyDescriptors();

                    if (pds != null) {
                        for (PropertyDescriptor pd : pds) {
                            if (provider.providesAdapter(type, pd.getName())) {
                                des.add(pd);
                            }
                        }
                    }
                }
            }
        }

        return des;
    }

    private static final class VendedAdapter {
        private final BeanAdapterProvider provider;
        private final String property;
        private final WeakReference<Object> adapter;

        public VendedAdapter(String property, BeanAdapterProvider provider,
            Object adapter) {
            this.property = property;
            this.adapter = new WeakReference<Object>(adapter);
            this.provider = provider;
        }

        public Object getAdapter() {
            return adapter.get();
        }

        public String getProperty() {
            return property;
        }

        public BeanAdapterProvider getProvider() {
            return provider;
        }
    }
}
