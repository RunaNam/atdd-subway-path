package wooteco.subway.dao;

import wooteco.subway.domain.Line;
import wooteco.subway.dto.LineRequest;

import java.util.List;
import java.util.Optional;

public interface LineDao {

    Long save(LineRequest lineRequest);

    Optional<Line> findById(Long id);

    List<Line> findAll();

    List<Integer> findExtraFareByIds(List<Long> ids);

    boolean hasLine(String name);

    void updateById(Long id, String name, String color, int extraFare);

    void deleteById(Long id);
}
