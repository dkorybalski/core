package pl.edu.amu.wmi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PointsMapper {

    @Named("PointsToPercent")
    default String mapPointsToPercent(Double points) {
        if (points == null) {
            return "0%";
        } else {
            return points * 100 + "%";
        }
    }

}
