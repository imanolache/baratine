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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;

import com.caucho.v5.util.L10N;
import com.caucho.v5.util.Utf8Util;
import io.baratine.web.Form;
import io.baratine.web.RequestWeb;

/**
 * Reads a body
 */
public class BodyResolverBase implements BodyResolver
{
  private static final L10N L = new L10N(BodyResolverBase.class);
  
  public static final String FORM_TYPE = "application/x-www-form-urlencoded";
  
  @Override
  public <T> T body(RequestWeb request, Class<T> type)
  {
    
    if (InputStream.class.equals(type)) {
      InputStream is = request.inputStream(); // new TempInputStream(_bodyHead);
      
      return (T) is;
    }
    else if (String.class.equals(type)) {
      InputStream is = request.inputStream();
      
      try { 
        return (T) Utf8Util.readString(is);
      } catch (IOException e) {
        throw new BodyException(e);
      }
    }
    else if (Reader.class.equals(type)) {
      InputStream is = request.inputStream();
      
      try { 
        return (T) new InputStreamReader(is, "utf-8");
      } catch (IOException e) {
        throw new BodyException(e);
      }
    }
    else if (Form.class.equals(type)) {
      String contentType = request.header("content-type");

      //TODO: parse and use the encoding of the content type e.g. application/x-www-form-urlencoded; UTF-8
      if (contentType == null || ! contentType.startsWith(FORM_TYPE)) {
        throw new IllegalStateException(L.l("Form expects {0}", FORM_TYPE));
      }
    
      return (T) parseForm(request);
    }
    /*
    else if (header("content-type").startsWith("application/json")) {
      TempInputStream is = new TempInputStream(_bodyHead);
      _bodyHead = _bodyTail = null;
      
      try { 
        Reader reader = new InputStreamReader(is, "utf-8");
        
        JsonReader isJson = new JsonReader(reader);
        return (X) isJson.readObject(type);
      } catch (IOException e) {
        throw new BodyException(e);
      }
    }
    */

    return bodyDefault(request, type);
  }
  
  public <T> T bodyDefault(RequestWeb request, Class<T> type)
  {
    String contentType = request.header("content-type");

    //TODO: parse and use the encoding of the content type e.g. application/x-www-form-urlencoded; UTF-8
    if (contentType.startsWith(FORM_TYPE)) {
      Form form = parseForm(request);
      
      return (T) formToBody(form, type);
    }
    
    throw new IllegalStateException(L.l("Unknown body type: " + type));
  }
  
  private <T> T formToBody(Form form, Class<T> type)
  {
    try {
      T bean = (T) type.newInstance();
      
      for (Field field : type.getDeclaredFields()) {
        String name = field.getName();
        
        String value = form.getFirst(name);
        
        if (value == null && name.startsWith("_")) {
          value = form.getFirst(name.substring(1));
        }
        
        if (value == null) {
          continue;
        }
        
        // XXX: introspection and conversion
        
        field.setAccessible(true);

        setFieldValue(bean, field, value);
      }
      
      return bean;
    } catch (Exception e) {
      throw new BodyException(e);
    }
  }

  private void setFieldValue(Object bean, Field field, String rawValue)
    throws IllegalAccessException
  {
    Class fieldType = field.getType();
    Object value = rawValue;

    if (fieldType == String.class) {

    }
    else if (boolean.class == fieldType || Boolean.class == fieldType) {
      value = Boolean.parseBoolean(rawValue);
    }
    else if (byte.class == fieldType || Byte.class == fieldType) {
      value = Byte.parseByte(rawValue);
    }
    else if (char.class == fieldType || Character.class == fieldType) {
      value = rawValue.charAt(0);
    }
    else if (short.class == fieldType || Short.class == fieldType) {
      value = Short.parseShort(rawValue);
    }
    else if (int.class == fieldType || Integer.class == fieldType) {
      value = Integer.parseInt(rawValue);
    }
    else if (long.class == fieldType || Long.class == fieldType) {
      value = Long.parseLong(rawValue);
    }
    else if (float.class == fieldType || Float.class == fieldType) {
      value = Float.parseFloat(rawValue);
    }
    else if (double.class == fieldType || Double.class == fieldType) {
      value = Double.parseDouble(rawValue);
    }
    else {
      throw new IllegalStateException(
        L.l("can't parse value to type {0}", field.getType()));
    }

    field.set(bean, value);
  }
  
  private Form parseForm(RequestWeb request)
  {
    InputStream is = request.inputStream();
    
    try { 
      FormImpl form = new FormImpl();
      
      FormBaratine.parseQueryString(form, is, "utf-8");
      
      return form;
    } catch (Exception e) {
      throw new BodyException(e);
    }
  }
}