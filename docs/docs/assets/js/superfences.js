function highlightLines(code, hlLines) {
    // inlining whitespace spans since they complicate the operation without showing up in the result
    const lines = code.innerHTML.replaceAll(/<span class="w">(\n\s*)<\/span>/g, "$1").split("\n");
    hlLines.split(" ").map(it => it - 1).forEach(hlLine => {
        const highlightedLine = lines[hlLine];
        if (highlightedLine) {
            lines[hlLine] = `<span class="hll">${highlightedLine}`;
            if (lines[hlLine + 1]) {
                lines[hlLine + 1] = `</span>${lines[hlLine + 1]}`
            } else {
                lines.push("</span>");
            }
        } else {
            console.warn(`Invalid highlighted line ${hlLine} for ${code}`)
        }
    });
    code.innerHTML = lines.join("\n");
}

document.querySelectorAll("ul.tabbed-code").forEach((tabs, i, parent) => {
    const tabbedSet = document.createElement("div");
    tabbedSet.classList.add("tabbed-set");
    Array.from(tabs.children).forEach((tab, j) => {
        const input = document.createElement("input");
        input.type = "radio";
        input.name = `__tabbed_${i+1}`;
        input.id = `__tabbed_${i+1}_${j+1}`;
        tabbedSet.append(input);
        const label = document.createElement("label");
        label.htmlFor = input.id;
        label.innerHTML = tab.firstChild.nodeName === "#text" ? tab.firstChild.textContent : tab.firstChild.innerHTML;
        tabbedSet.append(label);
        const tabbedContent = document.createElement("div");
        tabbedContent.classList.add("tabbed-content");
        let code = tab.querySelector(".highlighter-rouge");
        if (code !== null) {
            if (tab.dataset.hlLines) {
                highlightLines(code, tab.dataset.hlLines);
            }
            tabbedContent.appendChild(code);
        } else {
            console.warn("Missing code in tabs at " + tab);
        }
        tabbedSet.append(tabbedContent);
    });
    tabs.replaceWith(tabbedSet);
    tabbedSet.firstElementChild.click();
});
