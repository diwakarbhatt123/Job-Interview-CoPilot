import {NextApiRequest, NextApiResponse} from "next";
import {apiFetch} from "@/lib/api";
import {withAuthHeader} from "@/lib/api/withAuthHeader";
import {UnauthorizedError} from "@/error/UnauthorizedError";

export default withAuthHeader(async function (req: NextApiRequest, res: NextApiResponse, authHeaders: Record<string, string>) {
  if (req.method !== 'GET') {
    res.setHeader('Allow', ['GET']);
    return res.status(405).json({error: 'Method not allowed'});
  }

  try {
    const response = await apiFetch("/profile/profile/all", {
      method: 'GET',
      headers: authHeaders,
    })

    res.status(200).json(response)
  } catch (error: unknown) {
    if (error instanceof UnauthorizedError) {
      res.status(401).json({error: error.message});
    } else {
      const message =
        error instanceof Error ? error.message : 'Unexpected error'
      res.status(500).json({error: message});
    }
  }
})
