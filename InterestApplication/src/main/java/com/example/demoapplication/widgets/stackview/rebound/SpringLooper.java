/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.example.demoapplication.widgets.stackview.rebound;

/**
 * The spring looper is an interface for implementing platform-dependent run loops.
 */
public abstract class SpringLooper {

  protected BaseSpringSystem mSpringSystem;

  /**
   * Set the BaseSpringSystem that the SpringLooper will call back to.
   * @param springSystem the spring system to call loop on.
   */
  public void setSpringSystem(BaseSpringSystem springSystem) {
    mSpringSystem = springSystem;
  }

  public abstract void start();

  public abstract void stop();
}