/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def log = new File(basedir, 'build.log')
assert log.text.contains('Hello, world!')
