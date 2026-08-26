import DOMPurify from "dompurify";

export const MAX_RICH_TEXT_LENGTH = 10000;

const SANITIZE_CONFIG = {
    ALLOWED_TAGS: [
        "a", "b", "blockquote", "br", "div", "em",
        "h1", "h2", "h3", "h4", "hr", "i", "img",
        "li", "ol", "p", "s", "span", "strong",
        "table", "tbody", "td", "th", "thead",
        "tr", "u", "ul",
    ],
    ALLOWED_ATTR: [
        "align", "alt", "class", "colspan", "height",
        "href", "rel", "rowspan", "src", "style",
        "target", "title", "width",
    ],
};

export function sanitizeRichText(value) {
    const sanitized = DOMPurify.sanitize(
        String(value || ""),
        SANITIZE_CONFIG
    );

    const container = document.createElement("div");
    container.innerHTML = String(sanitized);

    container.querySelectorAll("a[href]").forEach(link => {
        const href = link.getAttribute("href")?.trim();

        if (!href) {
            link.removeAttribute("href");
            return;
        }

        if (/^https?:\/\//i.test(href)) {
            link.setAttribute("target", "_blank");
            link.setAttribute("rel", "noopener noreferrer");
        } else {
            link.removeAttribute("target");
            link.removeAttribute("rel");
        }
    });

    return container.innerHTML.trim();
}

export function getRichTextLength(value) {
    const container = document.createElement("div");

    container.innerHTML = String(value || "");

    return (container.textContent || "")
        .replace(/\u00a0/g, " ")
        .trim()
        .length;
}

export function hasRichTextContent(value) {
    const html = String(value || "");

    return getRichTextLength(html) > 0
        || /<(hr|img|table)\b/i.test(html);
}