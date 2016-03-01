/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.amp.proxy;

import io.baratine.service.Direct;
import io.baratine.service.Modify;
import io.baratine.service.Service;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.caucho.v5.amp.ServiceManagerAmp;
import com.caucho.v5.amp.actor.MethodAmpBase;
import com.caucho.v5.util.Hex;

/**
 * Creates AMP skeleton method.
 */
abstract class SkeletonMethodBase extends MethodAmpBase
{
  private static final MethodHandle _proxyFilterHandle;
  private static final MethodHandle _proxyReturnFilterHandle;
  
  private final Method _method;
  private final boolean _isDirect;
  private final boolean _isModify;
  
  protected SkeletonMethodBase(Method method)
  {
    _method = method;
    
    _isDirect = method.isAnnotationPresent(Direct.class);
    _isModify = method.isAnnotationPresent(Modify.class);
  }
  
  @Override
  public String name()
  {
    return _method.getName();
  }
  
  @Override
  public boolean isDirect()
  {
    return _isDirect;
  }
  
  @Override
  public boolean isModify()
  {
    return _isModify;
  }
  
  @Override
  public Annotation []getAnnotations()
  {
    return _method.getAnnotations();
  }
  
  @Override
  public Class<?> getReturnType()
  {
    return _method.getReturnType();
  }
  
  @Override
  public Class<?> []getParameterTypes()
  {
    return _method.getParameterTypes();
  }
  
  @Override
  public Type []getGenericParameterTypes()
  {
    return _method.getGenericParameterTypes();
  }
  
  @Override
  public Annotation [][]getParameterAnnotations()
  {
    return _method.getParameterAnnotations();
  }
  
  @Override
  public boolean isVarArgs()
  {
    return _method.isVarArgs();
  }
  
  protected MethodHandle filterMethod(ServiceManagerAmp rampManager,
                                      MethodHandle mh,
                                      Method method)
  {
    Class<?> []paramTypes = method.getParameterTypes();
    Annotation [][]paramAnns = method.getParameterAnnotations();
    Annotation []methodAnns = method.getAnnotations();
    
    if (paramAnns == null) {
      return mh;
    }

    for (int i = 0; i < paramTypes.length; i++) {
      Class<?> paramType = paramTypes[i];
      Annotation []paramAnn = paramAnns[i];
      
      if (paramAnn == null) {
        continue;
      }
      
      Service ampService = getAnnotation(Service.class, paramAnn);
      
      if (ampService == null) {
        continue;
      }
      
      FilterArgService filter = new FilterArgService(rampManager, paramType); 
      
      MethodHandle proxyFilter = _proxyFilterHandle.bindTo(filter);

      mh = MethodHandles.filterArguments(mh, i + 1, proxyFilter);
    }
    
    Service ampService = getAnnotation(Service.class, methodAnns);
    
    if (ampService != null) {
      FilterReturnService retFilter
        = new FilterReturnService(rampManager, method.getReturnType());

      MethodHandle retFilterHandle = _proxyReturnFilterHandle.bindTo(retFilter);

      mh = MethodHandles.filterReturnValue(mh, retFilterHandle);
    }
    
    return mh;
  }
  
  private <T> T getAnnotation(Class<T> annType, Annotation []anns)
  {
    for (Annotation ann : anns) {
      if (ann.annotationType().equals(annType)) {
        return (T) ann;
      }
    }
    
    return null;
  }
  
  protected CharSequence toList(Object []args)
  {
    if (args == null || args.length == 0) {
      return "()";
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    
    for (int i = 0; i < args.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }

      Object arg = args[i];
      
      if (arg instanceof byte[]) {
        sb.append(Hex.toShortHex((byte[]) args[i]));
      }
      else {
        sb.append(args[i]);
      }
    }
    
    sb.append(")");
    
    return sb;
  }

  static {
    MethodHandle argFilterHandle = null;
    MethodHandle retFilterHandle = null;
    
    try {
      Lookup lookup = MethodHandles.lookup();
      argFilterHandle = lookup.findVirtual(FilterArgService.class, 
                                           "filter", 
                                           MethodType.genericMethodType(1));
      
      retFilterHandle = lookup.findVirtual(FilterReturnService.class, 
                                           "filter", 
                                           MethodType.genericMethodType(1));
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    
    _proxyFilterHandle = argFilterHandle;
    _proxyReturnFilterHandle = retFilterHandle;
  }
}