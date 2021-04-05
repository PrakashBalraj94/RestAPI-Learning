package RestAssured.RestAssured;

import static io.restassured.RestAssured.*;

import java.io.File;
import java.util.List;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import pojo.Comments;
import pojo.Fields;
import pojo.GetComments;

public class restAPI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		restAPI r = new restAPI();
		RestAssured.baseURI = "http://localhost:8080/";

		//Login scenario
		SessionFilter session = new SessionFilter();
		String sessionResponse = given().relaxedHTTPSValidation().header("Content-Type", "application/json").body("{ \"username\": \"PrakashBalraj\", \"password\": \"PrakashBalraj@94\" }")
				.log().all().filter(session).when().post("rest/auth/1/session")
				.then().log().all().extract().response().asString();

		//Create defect
		String defectResponse = given().relaxedHTTPSValidation().header("Content-Type", "application/json").log().all().body("{\r\n" + 
				"    \"fields\": {\r\n" + 
				"       \"project\":\r\n" + 
				"       {\r\n" + 
				"          \"key\": \"RES\"\r\n" + 
				"       },\r\n" + 
				"       \"summary\": \"First defect created\",\r\n" + 
				"       \"description\": \"Creating first defect using the REST API\",\r\n" + 
				"       \"issuetype\": {\r\n" + 
				"          \"name\": \"Bug\"\r\n" + 
				"       }\r\n" + 
				"   }\r\n" + 
				"}").log().all().filter(session).when().post("rest/api/2/issue/").then().log().all().extract().response().asString();

		String firstDefectKey = r.jsonPathString(defectResponse, "key");

		//Add first comment
		String firstResponse = given().relaxedHTTPSValidation().pathParam("Key", firstDefectKey).header("Content-Type", "application/json").log().all().body("{\r\n" + 
				"    \"body\": \"This is first comment\",\r\n" + 
				"    \"visibility\": {\r\n" + 
				"        \"type\": \"role\",\r\n" + 
				"        \"value\": \"Administrators\"\r\n" + 
				"    }\r\n" + 
				"}").log().all().filter(session).when().post("rest/api/2/issue/{Key}/comment").then().assertThat().statusCode(201).extract().response().asString();

		String firstId = r.jsonPathString(firstResponse, "id");


		//Add second comment
		String comentResponse = given().relaxedHTTPSValidation().pathParam("Key", firstDefectKey).header("Content-Type", "application/json").log().all().body("{\r\n" + 
				"    \"body\": \"This is second comment\",\r\n" + 
				"    \"visibility\": {\r\n" + 
				"        \"type\": \"role\",\r\n" + 
				"        \"value\": \"Administrators\"\r\n" + 
				"    }\r\n" + 
				"}").log().all().filter(session).when().post("rest/api/2/issue/{Key}/comment").then().log().all().extract().response().asString();

		String commentId = r.jsonPathString(comentResponse, "id");

		//Update comment
		given().relaxedHTTPSValidation().pathParam("Key", firstDefectKey).pathParam("Key1", commentId).header("Content-Type", "application/json").log().all().body("{\r\n" + 
				"    \"body\": \"This is Second comment - Updated\",\r\n" + 
				"    \"visibility\": {\r\n" + 
				"        \"type\": \"role\",\r\n" + 
				"        \"value\": \"Administrators\"\r\n" + 
				"    }\r\n" + 
				"}").log().all().filter(session).when().put("rest/api/2/issue/{Key}/comment/{Key1}").then().log().all();

		//Delete comment
		/*given().pathParam("Key", firstDefectKey).pathParam("Key1", firstId).header("Content-Type", "application/json")
		.log().all().filter(session).when().delete("rest/api/2/issue/{Key}/comment/{Key1}").then().log().all();*/

		//Add Attachment
		given().relaxedHTTPSValidation().pathParam("Key", firstDefectKey).header("X-Atlassian-Token","no-check").filter(session)
		.header("Content-Type","multipart/form-data")
		.multiPart("file", new File("C:\\Users\\Prakash\\Desktop\\Defect_Attachment.docx"))
		.when().post("rest/api/2/issue/{Key}/attachments")
		.then().log().all();

		System.out.println("/**********************//////**********************/");
		//Get Issue
		String getIssueResponse = given().relaxedHTTPSValidation().pathParam("Key", firstDefectKey).log().all().filter(session)
				.queryParam("fields", "comment")
				.when().get("rest/api/2/issue/{Key}").then().log().all().extract().response().asString();

		System.out.println(getIssueResponse);
		JsonPath js1 = new JsonPath(getIssueResponse);
		int count = js1.getInt("fields.comment.comments.size()");
		
		for(int i =0; i<count; i++) {
			String id = js1.get("fields.comment.comments["+i+"].id").toString();
			if(id.equalsIgnoreCase(firstId)) {
				String message = js1.get("fields.comment.comments["+i+"].body").toString();
				System.out.println(message);
			}
		}
		
		RequestSpecification req = new RequestSpecBuilder().setRelaxedHTTPSValidation().addPathParam("Key", firstDefectKey).build();
		
		ResponseSpecification res = new ResponseSpecBuilder().expectContentType(ContentType.JSON).build();
		
		GetComments gs = given().spec(req).filter(session)
				.queryParam("fields", "comment").expect().defaultParser(Parser.JSON)
				.when().get("rest/api/2/issue/{Key}").then().spec(res).extract().as(GetComments.class);
		
		System.out.println(gs.getKey());
		System.out.println(gs.getId());
		System.out.println(gs.getSelf());
		
		List<Comments> fieldValue = gs.getFields().getComment().getComments();
		
		for(int i=0; i<fieldValue.size(); i++) {
			System.out.println(fieldValue.get(i).getId());
			System.out.println(fieldValue.get(i).getBody());
		}
		

	}

	public String jsonPathString(String response, String val) {
		JsonPath js = new JsonPath(response);
		String value = js.getString("\""+val+"\"");
		return value;

	}


}
