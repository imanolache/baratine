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

package com.caucho.v5.web.builder;

import com.caucho.v5.bartender.BartenderBuilder;
import com.caucho.v5.bartender.BartenderSystem;
import com.caucho.v5.bartender.journal.JournalSystem;
import com.caucho.v5.cli.args.ArgsBase;
import com.caucho.v5.kraken.KrakenSystem;
import com.caucho.v5.web.WebServerImpl;
import com.caucho.v5.web.cli.ArgsBaratine;
import com.caucho.v5.web.view.ViewJsonDefault;

import io.baratine.inject.Key;
import io.baratine.web.ViewWeb;

public class WebServerBuilderBaratine extends WebServerBuilderImpl
{
  @Override
  protected ArgsBase createArgs(String []argv)
  {
    return new ArgsBaratine(argv);
  }
  
  @Override
  public WebServerImpl build(WebServerBuilderImpl builder)
  {
    builder.bind(new Key<ViewWeb<Object>>() {}).to(ViewJsonDefault.class);

    //ServerBuilderBaratine serverBuilder;
    //serverBuilder = new ServerBuilderBaratine(builder.config());
    
    builder.init(()->{
      initBartender(builder);
    });
    
    builder.init(()->{
      KrakenSystem.createAndAddSystem(builder.serverSelf());
    });
    
    builder.init(()->{
      JournalSystem.createAndAddSystem();
    });
    
    //builder.serverBuilder(serverBuilder);
    
    //return new WebServerImpl(builder);
    
    //return builder.build();
    return super.build();
  }
  
  private void initBartender(WebServerBuilderImpl builder)
  {
    //String clusterId = "cluster";

    BartenderBuilder builderBar
      = BartenderSystem.newSystem(builder.config());

    //initTopologyStatic(builder);
    
    BartenderSystem system = builderBar.build();
    
    builder.serverSelf(system.serverSelf());
  }
}