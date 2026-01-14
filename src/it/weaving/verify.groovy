/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def log = new File(basedir, 'build.log')
assert log.text.contains('#name(): \'test\'')
assert log.text.contains('#exception(): thrown java.lang.IllegalStateException')
assert log.text.contains('com.jcabi.Counter: #ping(): attempt #1/4 failed with java.lang.IllegalStateException in')
