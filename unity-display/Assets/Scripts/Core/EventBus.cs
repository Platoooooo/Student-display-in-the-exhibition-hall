using System;
using System.Collections.Generic;

namespace Exhibition.Core
{
    public static class EventBus
    {
        private static readonly Dictionary<string, Action<object>> _events = new();

        public static void Subscribe(string evt, Action<object> handler)
        {
            if (_events.ContainsKey(evt)) _events[evt] += handler;
            else _events[evt] = handler;
        }

        public static void Unsubscribe(string evt, Action<object> handler)
        {
            if (_events.ContainsKey(evt)) _events[evt] -= handler;
        }

        public static void Emit(string evt, object data = null)
        {
            if (_events.TryGetValue(evt, out var handler)) handler?.Invoke(data);
        }
    }

    public static class GameEvents
    {
        public const string PlaylistLoaded     = "PlaylistLoaded";
        public const string PersonalModeStart  = "PersonalModeStart";
        public const string PersonalModeEnd    = "PersonalModeEnd";
        public const string CardChanged        = "CardChanged";
        public const string FaceDetected       = "FaceDetected";
    }
}