/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 *
 *    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "log4j" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation.  For more  information on the
 * Apache Software Foundation, please see <http://www.apache.org/>.
 *
 */

package org.apache.log4j.helpers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 * @author Ceki
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ReaderWriterLockTestCase extends TestCase {
  double value1 = 0;
  double value2 = 0;
  ReaderWriterLock lock = new ReaderWriterLock();

  /**
   * Constructor for ReaderWriterLockTestCasae.
   * @param arg0
   */
  public ReaderWriterLockTestCase(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    System.out.println("=====================");
    Logger root = Logger.getRootLogger();
    root.addAppender(new ConsoleAppender(new PatternLayout("%4r [%t] - %m%n")));
  }

  protected void tearDown() throws Exception {
    System.out.println("------------------------");
    LogManager.shutdown();
  }

  public void test1() {
    int maxReaders = 3;
    int maxWriters = 2;
    Thread[] threads = new Thread[maxReaders + maxWriters];

    for (int i = 0; i < maxReaders; i++) {
      threads[i] = new ReaderThread(i);
    }

    for (int i = 0; i < maxWriters; i++) {
      threads[maxReaders + i] = new WriterThread(i);
    }

    for (int i = 0; i < (maxWriters + maxReaders); i++) {
      threads[i].start();
    }

    for (int i = 0; i < (maxWriters + maxReaders); i++) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
      }
    }
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new ReaderWriterLockTestCase("test1"));

    return suite;
  }

  class ReaderThread extends Thread {
    ReaderThread(int i) {
      super("Reader-" + i);
    }

    public void run() {
      Logger root = Logger.getRootLogger();
      root.debug("In run()");

      for (int l = 0; l < 150; l++) {
        root.debug("Asking for read lock.");
        lock.getReadLock();
        root.debug("Got read lock.");
        root.debug("Value1 is " + value1);
        root.debug("Value2 is " + value2);
        try {sleep(10);} catch (InterruptedException e) {}
        root.debug("About to release read lock.");
        lock.releaseReadLock();
      }
    }
  }

  class WriterThread extends Thread {
    WriterThread(int i) {
      super("WRITER-" + i);
    }

    public void run() {
      Logger root = Logger.getRootLogger();
      root.debug("In run()");

      for (int l = 0; l < 50; l++) { 
        try {sleep(30);} catch (InterruptedException e) {}
        root.debug("Asking for write lock.");
        lock.getWriteLock();
        root.debug("Got write lock.");
        root.debug("About to increment values.");
        value1 += 1;
        value2 += 10;
        try {sleep(10);} catch (InterruptedException e) {}
        root.debug("About to release write lock.");
        lock.releaseWriteLock();

      }
    }
  }
}