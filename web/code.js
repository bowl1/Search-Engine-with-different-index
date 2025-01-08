let indexType = "inverted"; // Default index type
let currentPage = 1; // 当前页码
const resultsPerPage = 20; // 每页显示的结果数

// 点击 Try Forward Index 按钮
document.getElementById("use-forward-index").onclick = () => {
  indexType = "forward";
  alert("Forward Index selected.");

  // 清空 benchmark 结果和搜索结果区域
  resetResults();
};

// 点击 Try Inverted Index 按钮
document.getElementById("use-inverted-index").onclick = () => {
  indexType = "inverted";
  alert("Inverted Index selected.");

  // 清空 benchmark 结果和搜索结果区域
  resetResults();
};

// 点击 Search 按钮
document.getElementById("searchbutton").onclick = () => {
  const query = document.getElementById("searchbox").value.trim();

  if (!query) {
    alert("Please enter a search term!");
    return;
  }

  currentPage = 1; // 每次搜索重置为第一页
  performSearch(query, currentPage);
};

// 执行搜索并加载结果
function performSearch(query, page) {
  const offset = (page - 1) * resultsPerPage;

  // 执行搜索请求
  fetch(
    `/search?q=${encodeURIComponent(
      query
    )}&index=${indexType}&limit=${resultsPerPage}&offset=${offset}`
  )
    .then((response) => response.json())
    .then((data) => {
      console.log("Search Response:", data);
      // 解析响应数据
      const { totalResults, results } = data;

      // 显示总结果和分页信息
      displayPagination(query, totalResults, page);

      // 显示搜索结果
      if (results.length > 0) {
        const resultItems = results
          .map(
            (page) =>{
              const url = page.url.startsWith("http") ? page.url : `https://${page.url}`; // 确保 URL 完整
              return `<li><a href="${url}" target="_blank" rel="noopener noreferrer">${page.title}</a> - Score: ${
              page.score ? page.score.toFixed(2) : "N/A"
              }</li>`
      })
          .join("\n");
        document.getElementById(
          "urllist"
        ).innerHTML = `<ul>${resultItems}</ul>`;
      } else {
        document.getElementById("responsesize").innerHTML =
          "<p>No website contains the query word</p>";
        document.getElementById("urllist").innerHTML = `<ul></ul>`;
      }

      // 加载 benchmark 数据
      loadBenchmarkData();
    })
    .catch((error) => {
      console.error("Error fetching search results: ", error);
      document.getElementById(
        "urllist"
      ).innerHTML = `<p>Failed to fetch search results</p>`;
    });
}

// 加载 benchmark 数据
function loadBenchmarkData() {
  const benchmarkEndpoint =
    indexType === "forward" ? "/forwardIndexData" : "/invertedIndexData";

  fetch(benchmarkEndpoint)
    .then((response) => response.text())
    .then((benchmarkData) => {
      document.getElementById(
        "benchmark-results-container"
      ).innerHTML = `<pre>${benchmarkData}</pre>`;
    })
    .catch((error) => {
      console.error("Error fetching benchmark data: ", error);
      document.getElementById("benchmark-results-container").innerHTML =
        "<p>Failed to load benchmark data</p>";
    });
}


// 显示分页信息
function displayPagination(query, totalResults, currentPage) {
  console.log("Displaying pagination:", "Total Results:", totalResults, "Current Page:", currentPage);
  const totalPages = Math.ceil(totalResults / resultsPerPage);

  const paginationContainer = document.getElementById("pagination");
  paginationContainer.innerHTML = "";

  if (totalPages <= 1) {
      paginationContainer.style.display = "none";
      return;
  }

  paginationContainer.style.display = "block";

  const buttonsContainer = document.createElement("div");
  buttonsContainer.classList.add("pagination-buttons");

  // Previous Button
  if (currentPage > 1) {
      const prevButton = document.createElement("button");
      prevButton.textContent = "Previous";
      prevButton.onclick = () => performSearch(query, currentPage - 1);
      buttonsContainer.appendChild(prevButton);
  }

  // Page Number Buttons
  const visibleRange = 5;
  const startPage = Math.max(1, currentPage - Math.floor(visibleRange / 2));
  const endPage = Math.min(totalPages, startPage + visibleRange - 1);

  for (let i = startPage; i <= endPage; i++) {
      const pageButton = document.createElement("button");
      pageButton.textContent = i;
      pageButton.classList.add("page-button");
      if (i === currentPage) {
          pageButton.classList.add("active");
          pageButton.disabled = true;
      }
      pageButton.onclick = () => performSearch(query, i);
      buttonsContainer.appendChild(pageButton);
  }

  // Next Button
  if (currentPage < totalPages) {
      const nextButton = document.createElement("button");
      nextButton.textContent = "Next";
      nextButton.onclick = () => performSearch(query, currentPage + 1);
      buttonsContainer.appendChild(nextButton);
  }

  paginationContainer.appendChild(buttonsContainer);

  const infoContainer = document.createElement("div");
  infoContainer.classList.add("pagination-info");
  infoContainer.innerHTML = `<p>Total Results: ${totalResults}, Page ${currentPage} of ${totalPages}</p>`;
  paginationContainer.prepend(infoContainer);
}

// 重置搜索和 benchmark 区域
function resetResults() {
  document.getElementById("benchmark-results-container").innerHTML = "";
  document.getElementById("responsesize").innerHTML = "";
  document.getElementById("urllist").innerHTML = "";
}
