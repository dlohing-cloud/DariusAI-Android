export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const key = process.env.OPENAI_API_KEY;
  if (!key) {
    return res.status(500).json({ error: "OPENAI_API_KEY is not configured on the server." });
  }

  const body = req.body || {};
  const message = typeof body.message === "string" ? body.message.trim() : "";
  const workspace = typeof body.workspace === "string" ? body.workspace.trim() : "General";

  if (!message) {
    return res.status(400).json({ error: "message is required." });
  }

  const system = `You are Darius AI, a professional personal AI assistant for Darius.
The active workspace is: ${workspace}.
Be accurate, practical, clear and well-structured. For diplomacy, law, teaching and business tasks, adapt to the user's requested context. Do not claim to have performed actions you cannot perform.`;

  try {
    const response = await fetch("https://api.openai.com/v1/responses", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${key}`
      },
      body: JSON.stringify({
        model: process.env.OPENAI_MODEL || "gpt-6-luna",
        instructions: system,
        input: message,
        store: true
      })
    });

    const data = await response.json();

    if (!response.ok) {
      return res.status(response.status).json({
        error: data?.error?.message || "OpenAI request failed."
      });
    }

    return res.status(200).json({
      text: data?.output_text || "I received your request but no text response was returned.",
      response_id: data?.id || null
    });
  } catch (error) {
    return res.status(500).json({
      error: error instanceof Error ? error.message : "Unexpected server error."
    });
  }
}
