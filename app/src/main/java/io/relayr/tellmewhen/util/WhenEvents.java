package io.relayr.tellmewhen.util;

import io.relayr.tellmewhen.app.MainActivity;
import io.relayr.tellmewhen.model.Rule;

public class WhenEvents {

    public static class DoneEvent {
        public DoneEvent() {
        }
    }

    public static class DoneCreateEvent {
        public DoneCreateEvent() {
        }
    }

    public static class DoneEditEvent {
        public DoneEditEvent() {
        }
    }

    public static class StartEditEvent {
        private final Rule rule;

        public StartEditEvent(Rule rule) {
            this.rule = rule;
        }

        public Rule getRule() {
            return rule;
        }
    }

    public static class EditEvent {
        private final MainActivity.FragNames frag;

        public EditEvent(MainActivity.FragNames frag) {
            this.frag = frag;
        }

        public MainActivity.FragNames getFrag() {
            return frag;
        }
    }
}
