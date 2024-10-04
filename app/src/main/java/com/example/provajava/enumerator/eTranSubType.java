package com.example.provajava.enumerator;

public enum eTranSubType {
    NONE,
    SALARY{
        @Override
        public String toString() {
            return "Stipendio";
        }
    },
    OTHER_INCOME{
        @Override
        public String toString() {
            return "Altro";
        }
    },
    NECESSARY{
        @Override
        public String toString() {
            return "Necessaria";
        }
    },
    UNNECESSARY{
        @Override
        public String toString() {
            return "Superflua";
        }
    },
    EXTRA{
        @Override
        public String toString() {
            return "Extra";
        }
    },
    TRADE{
        @Override
        public String toString() {
            return "Investimento";
        }
    },
}

