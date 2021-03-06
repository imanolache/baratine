/*
 * Copyright (c) 1998-2016 Caucho Technology -- all rights reserved
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
 * @author Nam Nguyen
 */

package com.caucho.v5.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class StatementWrapper implements Statement
{
  private ConnectionWrapper _conn;
  private Statement _stmt;

  public StatementWrapper(ConnectionWrapper conn, Statement stmt)
  {
    _conn = conn;
    _stmt = stmt;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException
  {
    return _stmt.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException
  {
    return _stmt.isWrapperFor(iface);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException
  {
    return _stmt.executeQuery(sql);
  }

  @Override
  public int executeUpdate(String sql) throws SQLException
  {
    return _stmt.executeUpdate(sql);
  }

  @Override
  public void close() throws SQLException
  {
    _stmt.close();
  }

  @Override
  public int getMaxFieldSize() throws SQLException
  {
    return _stmt.getMaxFieldSize();
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException
  {
    _stmt.setMaxFieldSize(max);
  }

  @Override
  public int getMaxRows() throws SQLException
  {
    return _stmt.getMaxRows();
  }

  @Override
  public void setMaxRows(int max) throws SQLException
  {
    _stmt.setMaxRows(max);
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException
  {
    _stmt.setEscapeProcessing(enable);
  }

  @Override
  public int getQueryTimeout() throws SQLException
  {
    return _stmt.getQueryTimeout();
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException
  {
    _stmt.setQueryTimeout(seconds);
  }

  @Override
  public void cancel() throws SQLException
  {
    _stmt.cancel();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException
  {
    return _stmt.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException
  {
    _stmt.clearWarnings();
  }

  @Override
  public void setCursorName(String name) throws SQLException
  {
    _stmt.setCursorName(name);
  }

  @Override
  public boolean execute(String sql) throws SQLException
  {
    return _stmt.execute(sql);
  }

  @Override
  public ResultSet getResultSet() throws SQLException
  {
    return _stmt.getResultSet();
  }

  @Override
  public int getUpdateCount() throws SQLException
  {
    return _stmt.getUpdateCount();
  }

  @Override
  public boolean getMoreResults() throws SQLException
  {
    return _stmt.getMoreResults();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException
  {
    _stmt.setFetchDirection(direction);
  }

  @Override
  public int getFetchDirection() throws SQLException
  {
    return _stmt.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException
  {
    _stmt.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException
  {
    return _stmt.getFetchSize();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException
  {
    return _stmt.getResultSetConcurrency();
  }

  @Override
  public int getResultSetType() throws SQLException
  {
    return _stmt.getResultSetType();
  }

  @Override
  public void addBatch(String sql) throws SQLException
  {
    _stmt.addBatch(sql);
  }

  @Override
  public void clearBatch() throws SQLException
  {
    _stmt.clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException
  {
    return _stmt.executeBatch();
  }

  @Override
  public Connection getConnection() throws SQLException
  {
    return _conn;
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException
  {
    return _stmt.getMoreResults(current);
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException
  {
    return _stmt.getGeneratedKeys();
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
  {
    return _stmt.executeUpdate(sql, autoGeneratedKeys);
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
  {
    return _stmt.executeUpdate(sql, columnIndexes);
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException
  {
    return _stmt.executeUpdate(sql, columnNames);
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
  {
    return _stmt.execute(sql, autoGeneratedKeys);
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException
  {
    return _stmt.execute(sql, columnIndexes);
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException
  {
    return _stmt.execute(sql, columnNames);
  }

  @Override
  public int getResultSetHoldability() throws SQLException
  {
    return _stmt.getResultSetHoldability();
  }

  @Override
  public boolean isClosed() throws SQLException
  {
    return _stmt.isClosed();
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException
  {
    _stmt.setPoolable(poolable);
  }

  @Override
  public boolean isPoolable() throws SQLException
  {
    return _stmt.isPoolable();
  }

  @Override
  public void closeOnCompletion() throws SQLException
  {
    _stmt.closeOnCompletion();
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException
  {
    return _stmt.isCloseOnCompletion();
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _stmt + "]";
  }
}
