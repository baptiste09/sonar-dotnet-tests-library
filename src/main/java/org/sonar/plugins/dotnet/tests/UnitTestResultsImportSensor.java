/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.annotations.VisibleForTesting;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;

public class UnitTestResultsImportSensor implements Sensor {

  private final UnitTestResultsAggregator unitTestResultsAggregator;

  public UnitTestResultsImportSensor(UnitTestResultsAggregator unitTestResultsAggregator) {
    this.unitTestResultsAggregator = unitTestResultsAggregator;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return unitTestResultsAggregator.hasUnitTestResultsProperty();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    if (isReactorProject(project)) {
      analyze(context, new UnitTestResults());
    }
  }

  private static boolean isReactorProject(Project project) {
    return project.isRoot() && !project.getModules().isEmpty();
  }

  @VisibleForTesting
  void analyze(SensorContext context, UnitTestResults unitTestResults) {
    unitTestResultsAggregator.aggregate(unitTestResults);

    context.saveMeasure(CoreMetrics.TESTS, unitTestResults.tests());
    context.saveMeasure(CoreMetrics.TEST_SUCCESS_DENSITY, unitTestResults.passedPercentage());
    context.saveMeasure(CoreMetrics.TEST_ERRORS, unitTestResults.errors());
    context.saveMeasure(CoreMetrics.TEST_FAILURES, unitTestResults.failed());
    context.saveMeasure(CoreMetrics.SKIPPED_TESTS, unitTestResults.skipped());
  }

}
