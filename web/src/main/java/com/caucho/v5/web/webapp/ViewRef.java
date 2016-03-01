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

package com.caucho.v5.web.webapp;

import io.baratine.web.RequestWeb;
import io.baratine.web.ViewWeb;

import java.lang.reflect.Type;
import java.util.Objects;

import com.caucho.v5.inject.type.TypeRef;

/**
 * View with associated type meta-data
 */
class ViewRef<T>
{
  private final ViewWeb<T> _view;
  
  private final Class<T> _type;

  /**
   * Creates the view and analyzes the type
   */
  public ViewRef(ViewWeb<T> view)
  {
    Objects.requireNonNull(view);
    
    _view = view;

    TypeRef viewType = TypeRef.of(view.getClass()).to(ViewWeb.class);
    
    TypeRef typeRef = viewType.param(0);

    if (typeRef != null) {
      _type = (Class) typeRef.rawClass();
    }
    else {
      _type = (Class) Object.class;
    }
  }

  /**
   * Creates the view and analyzes the type
   */
  public ViewRef(ViewWeb<T> view, Type type)
  {
    Objects.requireNonNull(view);
    
    _view = view;

    TypeRef viewType = TypeRef.of(type).to(ViewWeb.class);
    
    TypeRef typeRef = viewType.param(0);

    if (typeRef != null) {
      _type = (Class) typeRef.rawClass();
    }
    else {
      _type = (Class) Object.class;
    }
  }
  
  /**
   * Creates the view and analyzes the type
   */
  public ViewRef(ViewWeb<T> view, Class<T> type)
  {
    Objects.requireNonNull(view);
    Objects.requireNonNull(type);
    
    _view = view;
    _type = type;
  }
  
  ViewWeb<T> view()
  {
    return _view;
  }
  
  Class<?> type()
  {
    return _type;
  }
  
  boolean render(RequestWeb req, Object value)
  {
    if (! _type.isAssignableFrom(value.getClass())) {
      return false;
    }
    else {
      return _view.render(req, (T) value);
    }
  }
}