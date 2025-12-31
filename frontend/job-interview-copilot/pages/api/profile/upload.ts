import { withAuthHeader } from '@/lib/api/withAuthHeader'
import { NextApiRequest, NextApiResponse } from 'next'
import { UnauthorizedError } from '@/error/UnauthorizedError'
import { apiFetchRaw } from '@/lib/api'
import formidable, { Files, Fields } from 'formidable'
import { readFile } from 'fs/promises'

export const config = {
  api: {
    bodyParser: false,
  },
}

function parseForm(
  req: NextApiRequest,
): Promise<{ fields: Fields; files: Files }> {
  const form = formidable({ multiples: false })
  return new Promise((resolve, reject) => {
    form.parse(req, (err, fields, files) => {
      if (err) reject(err)
      else resolve({ fields, files })
    })
  })
}

export default withAuthHeader(async function (
  req: NextApiRequest,
  res: NextApiResponse,
  authHeaders: Record<string, string>,
) {
  if (req.method !== 'POST') {
    res.setHeader('Allow', ['POST'])
    return res.status(405).json({ error: 'Method not allowed' })
  }

  try {
    const { fields, files } = await parseForm(req)
    const displayName = Array.isArray(fields.displayName)
      ? fields.displayName[0]
      : fields.displayName
    const sourceType = Array.isArray(fields.sourceType)
      ? fields.sourceType[0]
      : fields.sourceType
    const resumeFile = Array.isArray(files.resume)
      ? files.resume[0]
      : files.resume

    if (!displayName || !sourceType || !resumeFile) {
      return res.status(400).json({ error: 'Missing required fields.' })
    }

    const buffer = await readFile(resumeFile.filepath)
    const form = new FormData()
    form.append('displayName', String(displayName))
    form.append('sourceType', String(sourceType))
    form.append(
      'resume',
      new Blob([buffer], { type: 'application/pdf' }),
      resumeFile.originalFilename || 'resume.pdf',
    )

    const upstream = await apiFetchRaw('/profile/profile/upload', {
      method: 'POST',
      headers: {
        ...authHeaders,
      },
      body: form,
    })

    const contentType = upstream.headers.get('content-type') || ''
    if (contentType.includes('application/json')) {
      const data = await upstream.json()
      return res.status(upstream.status).json(data)
    }

    const text = await upstream.text()
    return res.status(upstream.status).send(text)
  } catch (error: unknown) {
    console.error(error)
    if (error instanceof UnauthorizedError) {
      res.status(401).json({ error: 'Unauthorized' })
    } else {
      const message =
        error instanceof Error ? error.message : 'Unexpected error'
      res.status(500).json({ error: message })
    }
  }
})
