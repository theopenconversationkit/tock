/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            tabbedContent.appendChild(code);
        } else {
            console.warn("Missing code in tabs at " + tab);
        }
        tabbedSet.append(tabbedContent);
    });
    tabs.replaceWith(tabbedSet);
    tabbedSet.firstElementChild.click();
});