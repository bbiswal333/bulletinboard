package com.sap.bulletinboard.ads.controllers;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bulletinboard.ads.models.Advertisement;
import com.sap.bulletinboard.ads.models.AdvertisementRepository;

/*
 * Use a path which does not end with a slash! Otherwise the controller is not reachable when not using the trailing
 * slash in the URL
 */
@RestController
@RequestMapping(path = AdvertisementController.PATH)
@RequestScope // @Scope(WebApplicationContext.SCOPE_REQUEST)
@Validated
public class AdvertisementController {
    public static final String PATH = "/api/v1/ads";
    public static final String PATH_PAGES = PATH + "/pages/";
    public static final int FIRST_PAGE_ID = 0;
    // allows server side optimization e.g. via caching
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AdvertisementRepository adRepository;

    @Inject
    public AdvertisementController(AdvertisementRepository repository) {
        this.adRepository = repository;
    }

    @GetMapping
    public ResponseEntity<AdvertisementListDto> advertisements() {
        return advertisementsForPage(FIRST_PAGE_ID);
    }

    @GetMapping("/pages/{pageId}") // not "public"
    public ResponseEntity<AdvertisementListDto> advertisementsForPage(@PathVariable("pageId") int pageId) {

        Page<Advertisement> page = adRepository.findAll(new PageRequest(pageId, DEFAULT_PAGE_SIZE));

        return new ResponseEntity<AdvertisementListDto>(new AdvertisementListDto(page.getContent()),
                buildLinkHeader(pageId, page), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public AdvertisementDto advertisementById(@PathVariable("id") @Min(0) Long id) {
        logger.info("method entry, GET: {}/{}", PATH, id);
        throwIfNonexisting(id);
        AdvertisementDto ad = new AdvertisementDto(adRepository.findOne(id));
        logger.info("returning: {}", ad);
        return ad;
    }

    /**
     * @RequestBody is bound to the method argument. HttpMessageConverter resolves method argument depending on the
     *              content type.
     */
    @PostMapping
    public ResponseEntity<AdvertisementDto> add(@Valid @RequestBody AdvertisementDto advertisement,
            UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {

        AdvertisementDto savedAdvertisement = new AdvertisementDto(adRepository.save(advertisement.toEntity()));

        UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}")
                .buildAndExpand(savedAdvertisement.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(uriComponents.getPath()));
        return new ResponseEntity<>(savedAdvertisement, headers, HttpStatus.CREATED);
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteAll() {
        adRepository.deleteAll();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        throwIfNonexisting(id);
        adRepository.delete(id);
    }

    @PutMapping("/{id}")
    public AdvertisementDto update(@PathVariable("id") long id, @RequestBody AdvertisementDto updatedAd) {
        throwIfInconsistent(id, updatedAd.getId());
        throwIfNonexisting(id);
        adRepository.save(updatedAd.toEntity());
        return new AdvertisementDto(adRepository.findOne(id)); // Note that EntityManager.merge might not update all
                                                               // fields such as createdAt
    }

    private static HttpHeaders buildLinkHeader(int pageId, Page<Advertisement> page) {
        HttpHeaders headers = new HttpHeaders();
        String link = "";

        if (page.hasPrevious()) {
            link += "<" + PATH_PAGES + (pageId - 1) + ">; rel=\"previous\"";
            if (!page.isLast())
                link += ", ";
        }
        if (page.hasNext()) {
            link += "<" + PATH_PAGES + (pageId + 1) + ">; rel=\"next\"";
        }
        if (!link.trim().isEmpty()) {
            headers.add(HttpHeaders.LINK, "Link: " + link);
        }
        return headers;
    }

    private void throwIfNonexisting(long id) {
        if (!adRepository.exists(id)) {
            NotFoundException notFoundException = new NotFoundException(id + " not found");
            logger.warn("request failed", notFoundException);
            throw notFoundException;
        }
    }

    private void throwIfInconsistent(Long expected, Long actual) {
        if (!expected.equals(actual)) {
            String message = String.format(
                    "bad request, inconsistent IDs between request and object: request id = %d, object id = %d",
                    expected, actual);
            throw new BadRequestException(message);
        }
    }

    public static class AdvertisementListDto {
        @JsonProperty("value")
        public List<AdvertisementDto> advertisements;

        public AdvertisementListDto(Iterable<Advertisement> ads) {
            this.advertisements = StreamSupport.stream(ads.spliterator(), false).map(AdvertisementDto::new)
                    .collect(Collectors.toList());
        }
    }
}
