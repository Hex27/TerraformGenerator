package org.terraform.command.contants;

public class InvalidArgumentException extends Exception {
	
	private String problem;
	public InvalidArgumentException(String problem){
		this.problem = problem;
	}
	
	public String getProblem(){
		return problem;
	}

}