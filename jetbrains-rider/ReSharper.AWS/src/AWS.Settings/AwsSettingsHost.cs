﻿using AWS.Daemon.Settings;
using AWS.Toolkit.Rider.Model;
using JetBrains.Annotations;
using JetBrains.Application.Settings;
using JetBrains.Lifetimes;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.DataContext;
using JetBrains.ReSharper.Daemon.Impl;

#if (PROFILE_2020_2 || PROFILE_2020_3 || PROFILE_2021_1) // TODO: Remove preprocessor conditions FIX_WHEN_MIN_IS_212
using JetBrains.ReSharper.Host.Features;
#else
using JetBrains.RdBackend.Common.Features;
#endif

namespace AWS.Settings
{
    [SolutionComponent]
    public class AwsSettingsHost
    {
        public AwsSettingsHost(Lifetime lifetime, [NotNull] ISolution solution, [NotNull] ISettingsStore settingsStore)
        {
            var model = solution.GetProtocolSolution().GetAwsSettingModel();

            var contextBoundSettingsStoreLive = settingsStore.BindToContextLive(lifetime, ContextRange.Smart(solution.ToDataContext()));

            model.ShowLambdaGutterMarks.Advise(lifetime, isEnabled =>
            {
                var entry = settingsStore.Schema.GetScalarEntry( (LambdaGutterMarkSettings s) => s.Enabled);
                contextBoundSettingsStoreLive.SetValue(entry, isEnabled, null);
                solution.GetComponent<DaemonImpl>().Invalidate();
            });
        }
    }
}
