const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Content-Type": "application/json; charset=utf-8",
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: corsHeaders });
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { headers: corsHeaders });
    if (request.method !== "POST") return json({ error: "Use POST." }, 405);
    if (!env.OPENAI_API_KEY) return json({ error: "AI backend is not configured." }, 503);

    try {
      const body = await request.json();
      const character = String(body.character || "Companion").slice(0, 60);
      const messages = Array.isArray(body.messages) ? body.messages.slice(-16) : [];
      if (!messages.length) return json({ error: "Message history is empty." }, 400);

      const input = [{
        role: "system",
        content: [{
          type: "input_text",
          text: `You are ${character}, a friendly, respectful AI companion. Answer the user's questions clearly and naturally in the language they use. Be honest when you do not know something. Do not claim to be a real person.`,
        }],
      }];

      for (const item of messages) {
        const role = item.role === "assistant" ? "assistant" : "user";
        const content = String(item.content || "").slice(0, 3000);
        if (content.trim()) input.push({ role, content: [{ type: "input_text", text: content }] });
      }

      const upstream = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${env.OPENAI_API_KEY}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: env.OPENAI_MODEL || "gpt-6-astra",
          input,
          max_output_tokens: 700,
        }),
      });

      const result = await upstream.json();
      if (!upstream.ok) {
        return json({ error: result.error?.message || "AI provider request failed." }, 502);
      }

      const reply = (result.output || [])
        .filter(item => item.type === "message")
        .flatMap(item => item.content || [])
        .filter(item => item.type === "output_text")
        .map(item => item.text)
        .join("\n")
        .trim();

      if (!reply) return json({ error: "AI returned an empty response." }, 502);
      return json({ reply });
    } catch (error) {
      return json({ error: error.message || "Unexpected backend error." }, 500);
    }
  },
};
