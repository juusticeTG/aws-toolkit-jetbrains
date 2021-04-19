// Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package software.aws.toolkits.jetbrains.services.ecs.exec

import com.intellij.openapi.actionSystem.AnActionEvent
import software.aws.toolkits.jetbrains.core.explorer.actions.SingleResourceNodeAction
import software.aws.toolkits.jetbrains.services.ecs.EcsServiceNode

class RunCommandInContainer:
    SingleResourceNodeAction<EcsServiceNode>("Run Command In Container", null) {
    override fun actionPerformed(selected: EcsServiceNode, e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
