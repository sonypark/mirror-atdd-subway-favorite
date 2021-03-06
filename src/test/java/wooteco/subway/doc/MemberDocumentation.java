package wooteco.subway.doc;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;

public class MemberDocumentation {
    public static RestDocumentationResultHandler createMember() {
        return document("members/create",
            requestFields(
                fieldWithPath("email").type(JsonFieldType.STRING)
                    .description("The user's email address"),
                fieldWithPath("name").type(JsonFieldType.STRING).description("The user's name"),
                fieldWithPath("password").type(JsonFieldType.STRING)
                    .description("The user's password")
            ),
            responseHeaders(
                headerWithName("Location").description("The user's location who just created")
            )
        );
    }

    public static RestDocumentationResultHandler getMemberByEmail() {
        return document("members/read",
            requestParameters(
                parameterWithName("email")
                    .description("The user's email address")
            ),
            responseFields(
                fieldWithPath("id").type(JsonFieldType.NUMBER)
                    .description("The user's identity"),
                fieldWithPath("email").type(JsonFieldType.STRING)
                    .description("The user's email address"),
                fieldWithPath("name").type(JsonFieldType.STRING)
                    .description("The user's name")
            )
        );
    }

    public static RestDocumentationResultHandler updateMember() {
        return document("members/update",
            pathParameters(
                parameterWithName("id")
                    .description("The user's identity")
            ),
            requestFields(
                fieldWithPath("name").type(JsonFieldType.STRING)
                    .description("The user's name"),
                fieldWithPath("password").type(JsonFieldType.STRING)
                    .description("The user's password")
            )
        );
    }

    public static RestDocumentationResultHandler deleteMember() {
        return document("members/delete",
            pathParameters(
                parameterWithName("id")
                    .description("The user's identity")
            )
        );
    }
}
