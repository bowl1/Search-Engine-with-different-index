let indexType = "inverted"; // InvertedIndex by default

document.getElementById("use-forward-index").onclick = () => {
  indexType = "forward";
  alert("Forward Index selected.");

    // 获取并显示 jmhdata.txt 的第一段内容 
    fetch("/forwardIndexData")
    .then((response) => response.text())
    .then((data) => {
      document.getElementById("benchmark-results-container").innerHTML =
        "<pre>" + data + "</pre>";  // 使用 <pre> 标签保留文本格式
    })
    .catch((error) => {
      console.error("Error fetching benchmark results from jmhdata.txt:", error);
      document.getElementById("benchmark-results-container").innerHTML =
        "<p>Failed to load benchmark data</p>";
    });
};

document.getElementById("use-inverted-index").onclick = () => {
  indexType = "inverted";
  alert("Inverted Index selected.");

  // 获取并显示 jmhdata.txt 的第二段内容 (section 2)
  fetch("/invertedIndexData")
    .then((response) => response.text())
    .then((data) => {
      document.getElementById("benchmark-results-container").innerHTML =
        "<pre>" + data + "</pre>";  // 使用 <pre> 标签保留文本格式
    })
    .catch((error) => {
      console.error("Error fetching benchmark results from jmhdata.txt:", error);
      document.getElementById("benchmark-results-container").innerHTML =
        "<p>Failed to load benchmark data</p>";
    });
};


document.getElementById("searchbutton").onclick = () => {
  const query = document.getElementById("searchbox").value.trim();
  fetch(`/search?q=${encodeURIComponent(query)}&index=${indexType}`)
    .then((response) => response.json())
    .then((data) => {
      if (data.length > 0) {
        document.getElementById(
          "responsesize"
        ).innerHTML = `<p>${data.length} websites retrieved</p>`;
        let results = data
          .map(
            (page) =>
              `<li><a href="${page.url}">${page.title}</a> - Score: ${
                page.score ? page.score.toFixed(2) : "N/A"
              }</li>`
          )
          .join("\n");
        document.getElementById("urllist").innerHTML = `<ul>${results}</ul>`;
      } else {
        document.getElementById("responsesize").innerHTML =
          "<p>No website contains the query word</p>";
        document.getElementById("urllist").innerHTML = `<ul></ul>`;
      }

    })
    .catch((error) => {
      console.error("Error fetching data: ", error);
    });
};
