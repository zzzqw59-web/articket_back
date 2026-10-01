package com.project.articket.common.util;


/**
 * 기상청 단기예보 API(VilageFcstInfoService) 전용
 * 위경도(WGS84) <-> 기상청 격자좌표(nx, ny) 변환 유틸리티.
 *
 * 기상청이 공식 배포하는 "단기예보 조회서비스 오픈API 활용가이드"에 포함된
 * Lambert Conformal Conic(LCC) 투영 변환식을 그대로 옮긴 것입니다.
 * 파라미터(RE, SLAT1, SLAT2, OLON, OLAT, XO, YO)는 기상청 고정값이라 변경하면 안 됩니다.
 *
 * 검증용 참고값: 서울(위도 37.579871128849334, 경도 126.98935225645432) -> nx=60, ny=127
 */
public class GridConverter {
    private static final double RE = 6371.00877;   // 지구 반경(km)
    private static final double GRID = 5.0;         // 격자 간격(km)
    private static final double SLAT1 = 30.0;       // 투영 위도1(degree)
    private static final double SLAT2 = 60.0;       // 투영 위도2(degree)
    private static final double OLON = 126.0;       // 기준점 경도(degree)
    private static final double OLAT = 38.0;        // 기준점 위도(degree)
    private static final double XO = 43;            // 기준점 X좌표(GRID)
    private static final double YO = 136;           // 기준점 Y좌표(GRID)

    private static final double DEGRAD = Math.PI / 180.0;
    private static final double RADDEG = 180.0 / Math.PI;

    private GridConverter() {
        // 유틸 클래스 - 인스턴스화 금지
    }

    /** 격자좌표(nx, ny) 결과를 담는 간단한 값 객체. */
    public record Grid(int nx, int ny) {
    }

    /** 위경도(WGS84)를 담는 간단한 값 객체. */
    public record LatLon(double lat, double lon) {
    }

    /**
     * 위경도 -> 기상청 격자좌표(nx, ny) 변환.
     *
     * @param lat 위도 (예: 전시장 API의 gpsY)
     * @param lon 경도 (예: 전시장 API의 gpsX)
     * @return nx, ny 격자좌표
     */
    public static Grid toGrid(double lat, double lon) {
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5)
                / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new Grid(nx, ny);
    }

    /**
     * 기상청 격자좌표(nx, ny) -> 위경도 변환 (역변환, 필요할 때만 사용).
     */
    public static LatLon toLatLon(int nx, int ny) {
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5)
                / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double xn = nx - XO;
        double yn = ro - (ny - YO);
        double ra = Math.sqrt(xn * xn + yn * yn);
        if (sn < 0.0) {
            ra = -ra;
        }
        double alat = Math.pow((re * sf / ra), (1.0 / sn));
        alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

        double theta;
        if (Math.abs(xn) <= 0.0) {
            theta = 0.0;
        } else {
            if (Math.abs(yn) <= 0.0) {
                theta = Math.PI * 0.5;
                if (xn < 0.0) {
                    theta = -theta;
                }
            } else {
                theta = Math.atan2(xn, yn);
            }
        }
        double alon = theta / sn + olon;

        return new LatLon(alat * RADDEG, alon * RADDEG);
    }

    // 간단 검증용 (실제 서비스 코드에는 포함하지 않아도 됩니다)
    public static void main(String[] args) {
        Grid grid = toGrid(37.579871128849334, 126.98935225645432);
        System.out.println("expected nx=60, ny=127 -> actual: " + grid);

        // 앞서 확인한 백남준아트센터 좌표로도 확인
        Grid njp = toGrid(37.26927634736376, 127.11037318643987);
        System.out.println("백남준아트센터 격자좌표: " + njp);
    }
}
