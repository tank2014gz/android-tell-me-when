package io.relayr.tellmewhen.util;

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
