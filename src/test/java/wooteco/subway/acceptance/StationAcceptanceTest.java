package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static wooteco.subway.acceptance.AcceptanceTestFixture.createStationResponse;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import wooteco.subway.dto.StationRequest;
import wooteco.subway.dto.StationResponse;

@DisplayName("지하철역 관련 기능")
public class StationAcceptanceTest extends AcceptanceTest {
    private final StationRequest 강남역 = new StationRequest("강남역");
    private final StationRequest 역삼역 = new StationRequest("역삼역");

    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // given
        // when
        ExtractableResponse<Response> response = createStationResponse(강남역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성할 때 예외를 발생시킨다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        createStationResponse(강남역);

        // when
        ExtractableResponse<Response> response = createStationResponse(강남역);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철역 목록을 조회한다.")
    @Test
    void getStations() {
        /// given
        ExtractableResponse<Response> createResponse1 = createStationResponse(강남역);
        ExtractableResponse<Response> createResponse2 = createStationResponse(역삼역);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get("/stations")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Stream.of(createResponse1, createResponse2)
                .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
                .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", StationResponse.class).stream()
                .map(StationResponse::getId)
                .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        ExtractableResponse<Response> createResponse = createStationResponse(강남역);

        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 지하철역을 제거할 때 예외를 발생시킨다.")
    @Test
    void deleteInvalidStation() {
        // given
        ExtractableResponse<Response> createResponse = createStationResponse(강남역);

        // when
        String uri = createResponse.header("Location");
        RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
