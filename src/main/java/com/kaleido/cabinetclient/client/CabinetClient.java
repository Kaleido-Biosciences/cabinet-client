/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.partitioningBy;

/**
 * The class that is used to interact with the Cabinet service. {@code @Bean}s of this class genericized on a
 * (@code domain} package class are made available for injection by class scanning {@code CabinetClientConfiguration}
 *
 * @param <E> The type of {@code domain} package object of the client
 */
public class CabinetClient<E> {

    private Logger log = LoggerFactory.getLogger(CabinetClient.class);

    @Autowired
    ApplicationContext context;

    @Autowired
    @Qualifier("cabinetRetryTemplate")
    private final RetryTemplate cabinetRetryTemplate;

    private final String endpoint;
    private final String searchEndpoint;
    @Autowired
    @Qualifier("cabinetRestTemplate")
    private final CabinetRestTemplate cabinetRestTemplate;
    private final Class<E> entityClass;

    private static final String PAGE = "page";
    private static final String SIZE = "size";
    public static final String QUERY = "query";
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = Integer.MAX_VALUE;
    private final List<String> fieldsInClass;

    //retains the type of E
    private ParameterizedTypeReference<List<E>> parameterizedTypeReference = new ParameterizedTypeReference<List<E>>() {
        @Override
        public Type getType() {
            return new ParameterizedTypeImpl((ParameterizedType) super.getType(),
                    new Type[]{entityClass});
        }
    };


    /**
     * A client to the Cabinet service parameterized on type {@code E}.
     *
     * @param endpoint     the endpoint extension (e.g. {@code /batches}) relevant to type {@code E}
     * @param cabinetRestTemplate a rest template
     * @param entityClass  the type token of type {@code E}. This is required to get around Java's erasure of generic
     *                     types at runtime.
     */
    public CabinetClient(String endpoint, String searchEndpoint, CabinetRestTemplate cabinetRestTemplate, RetryTemplate cabinetRetryTemplate, Class<E> entityClass) {
        this.endpoint = endpoint;
        this.searchEndpoint = searchEndpoint;
        this.cabinetRestTemplate = cabinetRestTemplate;
        this.entityClass = entityClass;
        this.cabinetRetryTemplate = cabinetRetryTemplate;

        this.fieldsInClass = new ArrayList<>();

        ReflectionUtils.doWithFields(entityClass, field -> {

            fieldsInClass.add(field.getName());

            //if the field is a class that has an Id field we need special handling
            if (Arrays.stream(field.getType().getDeclaredFields()).anyMatch(f -> f.getName().equals("id"))) {
                String fieldName = field.getName() + "Id";
                fieldsInClass.add(fieldName);
            }

        });
    }

    public String getEntityClassName() {
        return entityClass.toString();
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    /**
     * Find entities where a named field equals a defined value
     *
     * @param fieldName  the named field
     * @param fieldValue the value that the named field must have
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByFieldEquals(final String fieldName, final String fieldValue) {
        return findByFieldsEqual(Collections.singletonMap(fieldName, fieldValue));
    }

    /**
     * Find entities where a named field equals a defined value
     *
     * @param fieldName  the named field
     * @param fieldValue the named value
     * @param pageNumber the page number to start from (pages numbered from 0)
     * @param pageSize   the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code pageSize}
     */
    public ResponseEntity<List<E>> findByFieldEquals(final String fieldName, final String fieldValue, int pageNumber, int pageSize) {
        return findByFieldsEqual(Collections.singletonMap(fieldName, fieldValue), pageNumber, pageSize);
    }

    /**
     * Find entities where named fields equal their specified values
     *
     * @param fieldValueMap the map of fields to be search and their respective search values
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByFieldsEqual(final Map<String, String> fieldValueMap) {
        return findByFieldsEqual(fieldValueMap, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Find entities where named fields equal their specified values
     *
     * @param fieldValueMap the map of fields to be search and their respective search values
     * @param pageNumber    the page number to start from (pages numbered from 0)
     * @param pageSize      the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByFieldsEqual(
            final Map<String, String> fieldValueMap, final int pageNumber, final int pageSize) {
        return cabinetRetryTemplate.execute(arg0 -> {
            //check that the entityClass has the fields
            checkSearchFields(fieldValueMap.keySet());


            return cabinetRestTemplate.exchange(findByFieldsEqualUri(fieldValueMap, pageNumber, pageSize),
                    HttpMethod.GET, null, parameterizedTypeReference);
        });
    }

    /**
     * Find entities where a named field compares to a defined value
     *
     * @param fieldName  the named field
     * @param fieldValue the value that the named field must have for comparison
     * @param operator   the operator for which the value is compared
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByFieldWithOperator(final String fieldName, final String fieldValue, final String operator) {
        return findByFieldWithOperator(fieldName, fieldValue, operator, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Find entities where a named field compares to a defined value
     *
     * @param fieldName  the named field
     * @param fieldValue the named value
     * @param operator   the operator for which the value is compared
     * @param pageNumber the page number to start from (pages numbered from 0)
     * @param pageSize   the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code pageSize}
     */
    public ResponseEntity<List<E>> findByFieldWithOperator(final String fieldName, final String fieldValue, final String operator, int pageNumber, int pageSize) {
        Map<String, String> operatorMap = Stream.of(new String[][]{
                {"operator", operator},
                {"value", fieldValue},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        return findByFieldsWithOperators(Collections.singletonMap(fieldName, operatorMap), pageNumber, pageSize);
    }

    /**
     * Find entities where named fields compares to their specified values
     *
     * @param fieldValOperatorMap the map of fields to be search and their respective search values and operators
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */

    public ResponseEntity<List<E>> findByFieldsWithOperators(final Map<String, Map<String, String>> fieldValOperatorMap) {
        return findByFieldsWithOperators(fieldValOperatorMap, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Find entities where named fields compares to their specified values
     *
     * @param fieldValOperatorMap the map of fields to be search and their respective search values and operators
     * @param pageNumber          the page number to start from (pages numbered from 0)
     * @param pageSize            the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByFieldsWithOperators(final Map<String, Map<String, String>> fieldValOperatorMap, int pageNumber, int pageSize) {
        return cabinetRetryTemplate.execute(arg0 -> {
            checkSearchFields(fieldValOperatorMap.keySet());

            return cabinetRestTemplate.exchange(findByFieldsWithOperatorsUri(fieldValOperatorMap, pageNumber, pageSize),
                    HttpMethod.GET, null, parameterizedTypeReference);
        });
    }

    private void checkSearchFields(Set<String> strings) {
        //check that the entityClass has the fields
        //partition the keyset into fields present (true) and fields missing (false)
        Map<Boolean, List<String>> partitions =
                strings.stream().collect(partitioningBy(fieldsInClass::contains));

        if (partitions.get(Boolean.FALSE).size() > 0) {
            log.warn("{} does not contain the fields: {}", entityClass,
                    StringUtils.collectionToCommaDelimitedString(partitions.get(Boolean.FALSE)));
        }
    }

    /**
     * Find and entity using it's {@code id}.
     *
     * @param entityId the id of the entity
     * @return A response with the matching entity (if any)
     */
    public ResponseEntity<E> find(final Long entityId) {
        return cabinetRetryTemplate.execute(arg0 -> cabinetRestTemplate.getForEntity(endpoint + "/" + entityId, entityClass));
    }

    /**
     * Find and entity using methods defined in Cabinet resources.
     *
     * @param methodName the name of the method to search against (e.g. Sample api method '/label will' call case insensitive search function)
     * @param value the value to be passed to the method
     * @return A response with the matching entity (if any)
     */
    public ResponseEntity<E> findOneByMethod(final String methodName, final String value) {
        return cabinetRetryTemplate.execute(arg0 -> cabinetRestTemplate.getForEntity(endpoint + "/" + methodName + "/" + value, entityClass));
    }

    /**
     * Find all of an entity using methods defined in Kapture resources.
     *
     * @param methodName the name of the method to search against (e.g. Sample api method '/label will' call case insensitive search function)
     * @param value the value to be passed to the method
     * @return A response with the matching entity (if any)
     */
    public ResponseEntity<List<E>> findAllByMethod(final String methodName, final String value) {
        String uri = endpoint + "/" + methodName + (value != null ? "/" + value : "/");
        return cabinetRetryTemplate.execute(arg0 -> cabinetRestTemplate.exchange(uri,
                HttpMethod.GET, null, parameterizedTypeReference));
    }

    public ResponseEntity<List<E>> findAll() {
        return findAll(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public ResponseEntity<List<E>> findAll(int pageNumber, int pageSize) {
        return cabinetRetryTemplate.execute(arg0 -> cabinetRestTemplate.exchange(findByFieldsEqualUri(Collections.EMPTY_MAP, pageNumber, pageSize),
                HttpMethod.GET, null, parameterizedTypeReference));
    }

    /**
     * Save an entity. If the entity has an {@code id} an attempt will be made to update it. If it doesn't have an
     * {@code id} a new entity will be created.
     *
     * @param entity the entity to create or update
     * @return A response with a Body equal to the created or updated entity. If created the {@code id} will now be
     * set.
     */
    public ResponseEntity<E> save(@Valid E entity) {
        return cabinetRetryTemplate.execute(arg0 -> {
            //check if the id has already been set
            try {
                Long id = (Long) new PropertyDescriptor("id", entityClass).getReadMethod().invoke(entity);
                if (id == null) {
                    //no id, POST it
                    return cabinetRestTemplate.postForEntity(endpoint, entity, entityClass);
                } else {
                    //got an id, PUT it
                    HttpEntity<E> httpEntity = new HttpEntity<>(entity);
                    return cabinetRestTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, entityClass);
                }
            } catch (IntrospectionException | ReflectiveOperationException e) {
                log.error("Error accessing ID of entity {}", entity);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Save a list of entities. Behavior of the save (create vs update) will be done by Cabinet.
     *
     * @param entityList the list of an entity to create or update
     * @return A response with a Body equal to the created or updated entity. If created the {@code id} will now be
     * set.
     */
    public ResponseEntity<List<E>> saveAll(@Valid List<E> entityList) {
        return cabinetRetryTemplate.execute(arg0 -> {
                    HttpEntity<Object> requestEntity = new HttpEntity<Object>(entityList);
                    return cabinetRestTemplate
                            .exchange(endpoint + "/save-all", HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<E>>() {
                            });
                }
        );
    }

    /**
     * Deletes the entity with the matching {@code entityId}.
     *
     * @param entityId the id of the entity to delete
     */
    public void delete(final Long entityId) {
        cabinetRetryTemplate.execute(arg0 -> {
            cabinetRestTemplate.delete(endpoint + "/{id}", entityId);
            return null;
        });

    }


    /**
     * Convenience method to find entities by their {@code name} field. Most (but not all) Cabinet entities have a
     * {@code name} field.
     *
     * @param name the value of the {@code name} you want to search for
     * @return All entities of type {@code E} which have that name
     */
    public ResponseEntity<List<E>> findByName(String name) {
        return findByName(name, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Convenience method to find entities by their {@code name} field. Most (but not all) Cabinet entities have a
     * {@code name} field.
     *
     * @param name       the value of the {@code name} you want to search for
     * @param pageNumber the page number to start from (pages numbered from 0)
     * @param pageSize   the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByName(String name, int pageNumber, int pageSize) {
        return findByFieldEquals("name", name, pageNumber, pageSize);
    }

    /**
     * Convenience method to find an entity by a {@code name} field. Most (but not all) Cabinet entities have a
     * {@code name} field.
     *
     * @param name the value of the {@code name} you want to search for
     * @return The first entity found of type {@code E} which has that name
     */
    public Optional<E> findFirstByName(String name) {
        List<E> body = this.findByName(name, 0, 1).getBody();
        return Optional.ofNullable(body == null ? null : body.size() > 0 ? body.get(0) : null);
    }

    /**
     * Convenience method to find entities by their {@code label} field. Some (but not all) Cabinet entities have a
     * {@code label} field.
     *
     * @param label the value of the {@code label} you want to search for
     * @return All entities of type {@code E} which have that label
     */
    public ResponseEntity<List<E>> findByLabel(String label) {
        return findByFieldEquals("label", label);
    }

    /**
     * Convenience method to find entities by their {@code labe} field. Some (but not all) Cabinet entities have a
     * {@code label} field.
     *
     * @param label      the value of the {@code label} you want to search for
     * @param pageNumber the page number to start from (pages numbered from 0)
     * @param pageSize   the maximum number of entities in a page
     * @return the {@code List} of entities that match the search criteria up to {@code DEFAULT_SIZE}
     */
    public ResponseEntity<List<E>> findByLabel(String label, int pageNumber, int pageSize) {
        return findByFieldEquals("label", label, pageNumber, pageSize);
    }


    /**
     * Convenience method to find an entity by a {@code label} field. Some (but not all) Cabinet entities have a
     * {@code label} field.
     *
     * @param label the value of the {@code label} you want to search for
     * @return The first entity found of type {@code E} which has that label
     */
    public Optional<E> findFirstByLabel(String label) {
        List<E> body = findByLabel(label, 0, 1).getBody();
        return Optional.ofNullable(body == null ? null : body.size() > 0 ? body.get(0) : null);
    }

    /**
     * Search the elastic search backed index of Cabinet using a query string. The query string will also match any
     * nested objects that are serialized in the JSON that represents the object
     *
     * @param query the query
     * @return the first 20 {@code (DEFAULT_SIZE)} matching objects
     */
    public ResponseEntity<List<E>> search(String query) {
        return search(query, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Search the elastic search backed index of Cabinet using a query string. The query string will also match any
     * nested objects that are serialized in the JSON that represents the object
     *
     * @param query      the query string
     * @param pageNumber the page number to start from (pages numbered from 0)
     * @param pageSize   the maximum number of entities in a page
     * @return the list of matching entities
     */
    public ResponseEntity<List<E>> search(String query, int pageNumber, int pageSize) {
        return cabinetRetryTemplate.execute(arg0 -> {
            URI searchUri = searchUri(query, pageNumber, pageSize);
            return cabinetRestTemplate.exchange(searchUri, HttpMethod.GET, null, parameterizedTypeReference);
        });

    }

    /**
     * Builds the URI with appropriate query parameters to find entities by field
     *
     * @param fieldName the name of the field to search on
     * @param value     the value of the field to match
     * @return the URI that can be called to execute the search
     */
    protected URI findByFieldEqualsUri(String fieldName, String value) {
        return findByFieldsEqualUri(Collections.singletonMap(fieldName, value), DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Builds the URI with appropriate query parameters to find entities by one or more fields
     *
     * @param fieldValMap a map of fields to search and their respective values to match
     * @return the URI that can be called to execute the search
     */
    protected URI findByFieldsEqualUri(Map<String, String> fieldValMap, int pageNumber, int pageSize) {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint);

        //construct parts of the template to be expanded by the factory
        fieldValMap.forEach((key, value) -> builder.queryParam(key + ".equals", "{" + key + "}"));

        //add the page number and page size parameters to the query
        builder.queryParam(PAGE, pageNumber).queryParam(SIZE, pageSize);

        String uriTemplate = builder.build().toUriString();
        return factory.uriString(uriTemplate).build(fieldValMap);
    }

    /**
     * Builds the URI with appropriate query parameters to find entities by field
     *
     * @param fieldName the name of the field to search on
     * @param value     the value of the field to match
     * @param operator  the operator to match the value against
     * @return the URI that can be called to execute the search
     */
    protected URI findByFieldWithOperatorUri(String fieldName, String value, String operator) {
        return findByFieldWithOperatorUri(fieldName, value, operator, DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Builds the URI with appropriate query parameters to find entities by field
     *
     * @param fieldName the name of the field to search on
     * @param value     the value of the field to match
     * @param operator  the operator to match the value against
     * @return the URI that can be called to execute the search
     */
    protected URI findByFieldWithOperatorUri(String fieldName, String value, String operator, int pageNumber, int pageSize) {
        Map<String, String> operatorMap = Stream.of(new String[][]{
                {"operator", operator},
                {"value", value},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        return findByFieldsWithOperatorsUri(Collections.singletonMap(fieldName, operatorMap), pageNumber, pageSize);
    }

    /**
     * Builds the URI with appropriate query parameters to find entities by one or more fields
     *
     * @param fieldValOperatorMap a map of fields to search and their respective values to check against the mapped operators
     * @return the URI that can be called to execute the search
     */
    protected URI findByFieldsWithOperatorsUri(Map<String, Map<String, String>> fieldValOperatorMap, int pageNumber, int pageSize) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpoint);

        fieldValOperatorMap.forEach((key, value) -> builder.queryParam(key + "." + value.get("operator"), value.get("value")));

        //add the page number and page size parameters to the query
        builder.queryParam(PAGE, pageNumber).queryParam(SIZE, pageSize);

        return builder.build().toUri();
    }

    /**
     * Builds the search URI
     *
     * @param query      the query for the search
     * @param pageNumber the page of results to request (number from 0)
     * @param pageSize   the size of the results to request
     * @return the URI representing the request
     */
    protected URI searchUri(String query, int pageNumber, int pageSize) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(searchEndpoint);

        return builder.queryParam(QUERY, query)
                .queryParam(PAGE, pageNumber).queryParam(SIZE, pageSize).build().toUri();
    }

    public CabinetClient getClient(Class entityClass) throws ClassCastException {
        String beanName = StringUtils.uncapitalize(entityClass.getSimpleName()) + "Client";
        return (CabinetClient) context.getBean(beanName);
    }

}
