package se.adopi.edu.springcrudws;

import java.net.URI;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

	private final BookmarkRepository bookmarkRepository;
	private final AccountRepository accountRepository;

	@Autowired
	BookmarkRestController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
		this.bookmarkRepository = bookmarkRepository;
		this.accountRepository = accountRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	Collection<Bookmark> readBookmarks(@PathVariable String userId) {
		this.validateUser(userId);
		return this.bookmarkRepository.findByAccountUsername(userId);
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
		this.validateUser(userId);

		return this.accountRepository
				.findByUsername(userId)
				.map(account -> {
					Bookmark result = bookmarkRepository.save(new Bookmark(account,
							input.uri, input.description));

					URI location = ServletUriComponentsBuilder
						.fromCurrentRequest().path("/{id}")
						.buildAndExpand(result.getId()).toUri();

					return ResponseEntity.created(location).build();
				})
				.orElse(ResponseEntity.noContent().build());

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{bookmarkId}")
	Bookmark readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
		this.validateUser(userId);
		Bookmark bm = this.bookmarkRepository.findOne(bookmarkId);
		if (bm.getAccount().getUsername().equals(userId)) {
			return bm;
		} else {
			throw new SlutaSnokaException();
		}
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{bookmarkId}")
	ResponseEntity<?> updateBookmark(@PathVariable String userId,
			@PathVariable Long bookmarkId,
			@RequestBody Bookmark input) {
		this.validateUser(userId);
		Bookmark bm = this.bookmarkRepository.findOne(bookmarkId);
		if (bm.getAccount().getUsername().equals(userId)) {
			if (input.uri != null) bm.uri = input.uri;
			if (input.description != null) bm.description = input.description;
			this.bookmarkRepository.saveAndFlush(bm);
			return ResponseEntity.ok().build();
		} else {
			throw new SlutaSnokaException();
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{bookmarkId}")
	ResponseEntity<?> deleteBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
		this.validateUser(userId);
		Bookmark bm = this.bookmarkRepository.findOne(bookmarkId);
		if (bm.getAccount().getUsername().equals(userId)) {
			this.bookmarkRepository.delete(bm);
			return ResponseEntity.ok().build();
		} else {
			throw new SlutaSnokaException();
		}
	}

	private void validateUser(String userId) {
		this.accountRepository.findByUsername(userId).orElseThrow(
				() -> new UserNotFoundException(userId));
	}
}
