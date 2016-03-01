/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)(TM)
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

package com.caucho.v5.io;

import com.caucho.v5.util.FreeRing;

/**
 * Pooled temporary byte buffer.
 */
@SuppressWarnings("serial")
public class TempBufferLarge extends TempBuffer
{
  private static final FreeRing<TempBufferLarge> _freeList
    = new FreeRing<TempBufferLarge>(64);

  /**
   * Create a new TempBuffer.
   */
  public TempBufferLarge()
  {
    super(LARGE_SIZE);
  }
  
  /**
   * Allocate a TempBuffer, reusing one if available.
   */
  public static TempBufferLarge allocate()
  {
    TempBufferLarge next = _freeList.allocate();

    if (next == null) {
      next = new TempBufferLarge();
    }
    else {
      next.clearAllocate();
    }

    return next;
  }
  
  @Override
  public void freeSelf()
  {
    free(_freeList, this);
  }
  
  /**
   * Called on OOM to free buffers.
   */
  public static void clearFreeList()
  {
    while (_freeList.allocate() != null) {
    }
  }
}