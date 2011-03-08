package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.xml.AtomEntryFormatWriter;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.QueryInfo;

import com.sun.jersey.api.core.HttpContext;

public class PropertyRequestResource extends BaseResource {

	private static final Logger log =
			Logger.getLogger(PropertyRequestResource.class.getName());

	@PUT
	public Response updateEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName,
			final @PathParam("id") String id,
			final @PathParam("navProp") String navProp) {

		// throw new UnsupportedOperationException("Not supported yet.");
		log.info("NavProp: updateEntityNot supported yet.");
		return Response.ok().build();
	}

	@POST
	public Response mergeEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			@Context HttpHeaders headers,
			final @PathParam("entitySetName") String entitySetName,
			final @PathParam("id") String id,
			final @PathParam("navProp") String navProp) {

		if (!"MERGE".equals(context.getRequest().getHeaderValue(
				ODataConstants.Headers.X_HTTP_METHOD))) {
			
			OEntity entity = getRequestEntity(context.getRequest(),producer.getMetadata(),entitySetName);
			Object idObject = OptionsQueryParser.parseIdObject(id);
			EntityResponse response = producer.createEntity(entitySetName, idObject, navProp, entity);

	        if (response == null) {
	            return Response.status(Status.NOT_FOUND).build();
	        }
	        
	        //	TODO support JSON too
	        StringWriter sw = new StringWriter();
			String entryId = new AtomEntryFormatWriter().writeAndReturnId(
					context.getUriInfo(), 
					sw, 
					response);
			
			String responseEntity = sw.toString();

			return Response
					.ok(responseEntity, ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8)
					.status(Status.CREATED)
					.location(URI.create(entryId))
					.header(ODataConstants.Headers.DATA_SERVICE_VERSION,
							ODataConstants.DATA_SERVICE_VERSION).build();
		}

		throw new UnsupportedOperationException("Not supported yet.");
	}

	@DELETE
	public Response deleteEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName,
			final @PathParam("id") String id,
			final @PathParam("navProp") String navProp) {

		throw new UnsupportedOperationException("Not supported yet.");
	}

	@GET
	@Produces({
			ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
			ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
			ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
	public Response getNavProperty(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName,
			final @PathParam("id") String id,
			final @PathParam("navProp") String navProp,
			final @QueryParam("$inlinecount") String inlineCount,
			final @QueryParam("$top") String top,
			final @QueryParam("$skip") String skip,
			final @QueryParam("$filter") String filter,
			final @QueryParam("$orderby") String orderBy,
			final @QueryParam("$format") String format,
			final @QueryParam("$callback") String callback,
			final @QueryParam("$skiptoken") String skipToken,
			final @QueryParam("$expand") String expand,
			final @QueryParam("$select") String select) {

		QueryInfo query = new QueryInfo(
				OptionsQueryParser.parseInlineCount(inlineCount),
				OptionsQueryParser.parseTop(top),
				OptionsQueryParser.parseSkip(skip),
				OptionsQueryParser.parseFilter(filter),
				OptionsQueryParser.parseOrderBy(orderBy),
				OptionsQueryParser.parseSkipToken(skipToken),
				OptionsQueryParser.parseCustomOptions(context),
				OptionsQueryParser.parseSelect(expand),
				OptionsQueryParser.parseSelect(select));

		Object idObject = OptionsQueryParser.parseIdObject(id);
		final BaseResponse response = producer.getNavProperty(
				entitySetName,
				idObject,
				navProp,
				query);

		if (response == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		StringWriter sw = new StringWriter();
		FormatWriter<?> fwBase;
		if (response instanceof PropertyResponse) {
			FormatWriter<PropertyResponse> fw =
				FormatWriterFactory.getFormatWriter(
						PropertyResponse.class,
						context.getRequest().getAcceptableMediaTypes(),
						format,
						callback);

			fw.write(context.getUriInfo(), sw, (PropertyResponse)response);
			fwBase = fw;
		} else if (response instanceof EntityResponse) {
			FormatWriter<EntityResponse> fw =
					FormatWriterFactory.getFormatWriter(
							EntityResponse.class,
							context.getRequest().getAcceptableMediaTypes(),
							format,
							callback);

			fw.write(context.getUriInfo(), sw, (EntityResponse)response);
			fwBase = fw;
		} else if (response instanceof EntitiesResponse){
			FormatWriter<EntitiesResponse> fw =
					FormatWriterFactory.getFormatWriter(
							EntitiesResponse.class,
							context.getRequest().getAcceptableMediaTypes(),
							format,
							callback);

			fw.write(context.getUriInfo(), sw, (EntitiesResponse) response);
			fwBase = fw;
		} else {
			throw new UnsupportedOperationException("Unknown BaseResponse type: " + response.getClass().getName());
		}
		
		String entity = sw.toString();
		return Response.ok(
				entity,
				fwBase.getContentType()).header(
				ODataConstants.Headers.DATA_SERVICE_VERSION,
				ODataConstants.DATA_SERVICE_VERSION).build();
	}
}