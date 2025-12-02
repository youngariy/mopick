# Mopick (모픽)



**Mopick**은 좋아하는 영화와 TV 프로그램을 발견하고, 추적하고, 자신만의 감상을 기록할 수 있도록 도와주는 안드로이드 애플리케이션입니다. The Movie Database (TMDb)의 방대한 데이터를 활용하여 다채로운 콘텐츠를 탐색하고 취향에 맞는 작품을 찾아보세요.



---

## 📸 주요 화면

<table>
  <tr>
    <td align="center"><strong>영화 탐색</strong></td>
    <td align="center"><strong>검색</strong></td>
    <td align="center"><strong>즐겨찾기</strong></td>
  </tr>
  <tr>
    <td><img src="images/movies.gif" alt="영화 탐색 GIF" width="250"/></td>
    <td><img src="images/search.gif" alt="검색 GIF" width="250"/></td>
    <td><img src="images/favorites.gif" alt="즐겨찾기 GIF" width="250"/></td>
  </tr>
</table>


---

## ✨ 주요 기능

Mopick은 다음과 같은 다양한 기능을 제공합니다.

*   **🎬 영화 및 TV 프로그램 탐색**:
    *   **현재 상영중인 영화**: 지금 영화관에서 상영 중인 최신 영화들을 확인하세요.
    *   **인기 작품**: 많은 사람들이 즐겨보는 인기 영화와 TV 프로그램을 둘러보세요.
    *   **개봉 예정 영화**: 곧 개봉할 기대되는 신작들을 미리 만나보세요.
    *   **최고 평점 작품**: 높은 평가를 받은 명작들을 찾아보세요.
    *   **오늘 방영 TV 프로그램**: 오늘 방영되는 TV 프로그램 목록을 놓치지 마세요.

*   **🔍 강력한 검색**:
    *   영화, TV 프로그램, 그리고 배우 및 제작진(인물)까지 한 번에 검색할 수 있습니다.

*   **❤️ 즐겨찾기**:
    *   마음에 드는 영화와 TV 프로그램을 '즐겨찾기'에 추가하여 손쉽게 관리하세요.

*   **🎰 랜덤 추천**:
    *   무엇을 볼지 고민될 때, 화면을 당겨서 Mopick의 랜덤 추천을 받아보세요!

*   **✍️ 짧은 감상**:
    *   시청한 작품에 대해 나만의 평점, 기분, 그리고 짧은 메모를 기록하고 언제든지 다시 확인하세요.

*   **🌐 다국어 지원**:
    *   한국어, 영어, 일본어, 중국어를 지원하여 더욱 편리하게 사용할 수 있습니다.


---

## 🛠️ 기술 스택 및 라이브러리

이 프로젝트는 다음과 같은 기술과 라이브러리를 사용하여 개발되었습니다.

*   **언어**: [Java](https://www.java.com)
*   **아키텍처**: Model-View-ViewModel (MVVM)
*   **네트워킹**: [Retrofit2](https://square.github.io/retrofit/) - TMDb API와의 통신
*   **JSON 파싱**: [Gson](https://github.com/google/gson)
*   **이미지 로딩**: [Glide](https://github.com/bumptech/glide)
*   **UI 컴포넌트**:
    *   [AndroidX AppCompat, Material Components, RecyclerView, CardView](https://developer.android.com/jetpack/androidx)
    *   [SmartTabLayout](https://github.com/ogaclejapan/SmartTabLayout) - 탭 레이아웃 구현
*   **데이터 소스**: [The Movie Database (TMDb)](https://www.themoviedb.org/)


---

## ⚙️ 빌드 방법

1.  이 저장소를 클론합니다.
    ```bash
    git clone https://github.com/your-username/mopick.git
    ```
2.  Android Studio에서 프로젝트를 엽니다.
3.  `app/src/main/res/values/strings.xml` 파일에 있는 `MOVIE_DB_API_KEY`를 자신의 [TMDb API 키](https://developers.themoviedb.org/3/getting-started/introduction)로 교체해야 할 수 있습니다.
4.  Gradle 동기화가 완료되면 `app` 모듈을 빌드하고 실행합니다.

---

## 📄 라이선스

이 프로젝트는 [Apache License 2.0](LICENSE)에 따라 라이선스가 부여됩니다.

```
Copyright 2021 youngariy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
