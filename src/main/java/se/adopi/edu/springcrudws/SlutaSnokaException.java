package se.adopi.edu.springcrudws;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class SlutaSnokaException extends RuntimeException {
	public SlutaSnokaException() {
		super("GO AWAY!");
	}
}
