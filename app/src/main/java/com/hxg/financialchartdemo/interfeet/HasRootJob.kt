package com.hxg.financialchartdemo.interfeet

import kotlinx.coroutines.experimental.Job

interface HasRootJob {
    var rootJob: Job
}