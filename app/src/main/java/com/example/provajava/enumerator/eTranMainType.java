package com.example.provajava.enumerator;

public enum eTranMainType {
    INCOME{
        @Override
        public String toString() {
            return "Entrata";
        }
    },
    EXPENSE{
        @Override
        public String toString() {
            return "Uscita";
        }
    }
}
