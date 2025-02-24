/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.version;

/**
 * Current version of the project. Generated from a template at build time.
 * @since 0.23
 */
public enum Version {
    /**
     * Current version.
     */
    CURRENT("${project.version}", "${buildNumber}");

    /**
     * Project version.
     */
    private final String version;

    /**
     * Build number.
     */
    private final String build;

    /**
     * Public ctor.
     * @param ver Maven's project.version property
     * @param buildnum Maven's buildNumber property created with
     *  buildnumber-maven-plugin
     */
    Version(final String ver, final String buildnum) {
        this.version = ver;
        this.build = buildnum;
    }

    /**
     * Returns project version number.
     * @return Project version number
     */
    public String projectVersion() {
        return this.version;
    }

    /**
     * Returns project build number.
     * @return Build number
     */
    public String buildNumber() {
        return this.build;
    }
}
