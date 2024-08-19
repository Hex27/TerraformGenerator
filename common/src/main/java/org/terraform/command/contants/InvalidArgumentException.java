package org.terraform.command.contants;

public class InvalidArgumentException extends Exception {

    private final String problem;

    public InvalidArgumentException(String problem) {
        this.problem = problem;
    }

    public String getProblem() {
        return problem;
    }

}