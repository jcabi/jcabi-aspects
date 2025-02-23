/**
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def log = new File(basedir, 'build.log')
assert log.text.contains(
    '\'QuietlyNonVoid.foo\' annotated with @Quietly does not return void'
)
assert !log.text.contains(
    '\'QuietlyVoid.foo\' annotated with @Quietly does not return void'
)
assert log.text.contains(
    '\'AsyncInvalid.foo\' annotated with @Async does not return void or Future'
)
assert !log.text.contains('\'AsyncValid.returnsVoid\'')
assert !log.text.contains('\'AsyncValid.returnsFuture\'')
