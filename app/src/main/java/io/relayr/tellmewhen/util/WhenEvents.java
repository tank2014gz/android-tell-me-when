package io.relayr.tellmewhen.util;

import io.relayr.tellmewhen.consts.FragmentName;

public class WhenEvents {

    public static class ShowFragment {
        private final FragmentName name;

        public ShowFragment(FragmentName name) {
            this.name = name;
        }

        public FragmentName getName() {
            return name;
        }
    }

    public static class BackPressed {
        public BackPressed(){
        }
    }
}
