import {createHash} from "node:crypto";

export type DocumentBlock =
    | { type: "heading"; level: number; text: string }
    | { type: "paragraph"; text: string }
    | { type: "list"; style: "unordered" | "ordered"; items: string[] }
    | { type: "code"; language?: string; code: string }
    | { type: "blockquote"; text: string };

export type ParsedDocument = {
    title?: string;
    contentJson: {
        type: "document";
        schemaVersion: 1;
        blocks: DocumentBlock[];
    };
    plainText: string;
    tableOfContents: Array<{ level: number; text: string }>;
    contentHash: string;
};

export function parseMarkdown(markdown: string): ParsedDocument {
    const lines = markdown.replace(/\r\n/g, "\n").split("\n");
    const blocks: DocumentBlock[] = [];
    const tableOfContents: Array<{ level: number; text: string }> = [];
    const plainTextParts: string[] = [];
    let title: string | undefined;
    let index = 0;

    while (index < lines.length) {
        const line = lines[index];
        const trimmed = line.trim();

        if (!trimmed) {
            index += 1;
            continue;
        }

        if (trimmed.startsWith("```")) {
            const language = trimmed.slice(3).trim() || undefined;
            const codeLines: string[] = [];
            index += 1;
            while (index < lines.length && !lines[index].trim().startsWith("```")) {
                codeLines.push(lines[index]);
                index += 1;
            }
            blocks.push({type: "code", language, code: codeLines.join("\n")});
            plainTextParts.push(codeLines.join("\n"));
            index += 1;
            continue;
        }

        const heading = /^(#{1,6})\s+(.+)$/.exec(trimmed);
        if (heading) {
            const level = heading[1].length;
            const text = heading[2].trim();
            blocks.push({type: "heading", level, text});
            tableOfContents.push({level, text});
            plainTextParts.push(text);
            title ??= level === 1 ? text : undefined;
            index += 1;
            continue;
        }

        if (/^>\s?/.test(trimmed)) {
            const text = trimmed.replace(/^>\s?/, "");
            blocks.push({type: "blockquote", text});
            plainTextParts.push(text);
            index += 1;
            continue;
        }

        if (/^[-*+]\s+/.test(trimmed)) {
            const items: string[] = [];
            while (index < lines.length && /^[-*+]\s+/.test(lines[index].trim())) {
                items.push(lines[index].trim().replace(/^[-*+]\s+/, ""));
                index += 1;
            }
            blocks.push({type: "list", style: "unordered", items});
            plainTextParts.push(...items);
            continue;
        }

        if (/^\d+\.\s+/.test(trimmed)) {
            const items: string[] = [];
            while (index < lines.length && /^\d+\.\s+/.test(lines[index].trim())) {
                items.push(lines[index].trim().replace(/^\d+\.\s+/, ""));
                index += 1;
            }
            blocks.push({type: "list", style: "ordered", items});
            plainTextParts.push(...items);
            continue;
        }

        const paragraphLines = [trimmed];
        index += 1;
        while (
            index < lines.length &&
            lines[index].trim() &&
            !/^(#{1,6})\s+/.test(lines[index].trim()) &&
            !/^[-*+]\s+/.test(lines[index].trim()) &&
            !/^\d+\.\s+/.test(lines[index].trim()) &&
            !/^>\s?/.test(lines[index].trim()) &&
            !lines[index].trim().startsWith("```")
            ) {
            paragraphLines.push(lines[index].trim());
            index += 1;
        }
        const text = paragraphLines.join(" ");
        blocks.push({type: "paragraph", text});
        plainTextParts.push(text);
    }

    const plainText = plainTextParts.join("\n").trim();
    return {
        title,
        contentJson: {
            type: "document",
            schemaVersion: 1,
            blocks,
        },
        plainText,
        tableOfContents,
        contentHash: `sha256:${createHash("sha256").update(markdown).digest("hex")}`,
    };
}
